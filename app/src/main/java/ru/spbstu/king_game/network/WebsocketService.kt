package ru.spbstu.king_game.network

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.*
import ru.spbstu.king_game.common.utils.Logger

class WebsocketService(
    private val client: HttpClient = NetworkProvider.client
) {

    private var session: DefaultClientWebSocketSession? = null
    private var webSocketConnectionJob: Job? = null
    private val sendCommandFlow = MutableSharedFlow<WebsocketRequest>(1)
    private val eventFlow = MutableSharedFlow<WebsocketResponse>()

    init {
        start()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun startWebsocket() {
        session = client.webSocketSession(NetworkProvider.WEBSOCKET_URL)
        val session = session!!
        Logger.i("WebSocket: started")
        session.closeReason.invokeOnCompletion {
            session.launch {
                eventFlow.emit(WebsocketResponse.Error(ErrorType.CONNECTION_ERROR))
            }
            if (it is ClosedReceiveChannelException || it is CancellationException) {
                Logger.i("WebSocket: closed normally ${it.stackTraceToString()}")
                return@invokeOnCompletion
            }
            Logger.e("WebSocket: closed with exception ${it?.stackTraceToString()}")
        }
        session.incoming.consumeAsFlow().onEach { frame ->
            Logger.i("WebSocket: incoming ${if (frame is Frame.Text) frame.readText() else frame.toString()}")
            val converter = session.converter
            if (converter == null || !converter.isApplicable(frame)) {
                Logger.i("WebSocket: received not applicable frame: ${frame.frameType}")
                return@onEach
            }
            val charset = session.call.request.headers.suitableCharset()
            try {
                val event = converter.deserialize<WebsocketResponse.UpdateGameState>(frame, charset)
                Logger.i("WebSocket: message received $event")
                eventFlow.emit(event)
            } catch (e: Exception) {
                Logger.e("WebSocket: error while parse gameState $e, trying to parse session")
                val event = converter.deserialize<WebsocketResponse.OpenSession>(frame, charset)
                eventFlow.emit(event)
                Logger.e("WebSocket: session successfully parsed")
            }
        }.launchIn(session)
        sendCommandFlow.asSharedFlow().onEach {
            if (session.outgoing.isClosedForSend) {
                Logger.i("WebSocket: closed for send")
                return@onEach
            }
            Logger.i("WebSocket: message sent $it")
            session.sendSerialized(it)
        }.collect()
    }

    fun close() {
        CoroutineScope(Dispatchers.IO).launch {
            session?.close()
        }
    }

    fun sendCommand(command: WebsocketRequest) {
        if (!isConnected()) {
            start()
        }
        CoroutineScope(Dispatchers.IO).launch {
            sendCommandFlow.emit(command)
        }
    }

    private fun start() {
        val handler = CoroutineExceptionHandler { context, exception ->
            CoroutineScope(context).launch {
                Logger.e("WebSocket: got server error with exception ${exception.stackTraceToString()}")
                eventFlow.emit(WebsocketResponse.Error(ErrorType.SERVER_ERROR))
            }
        }
        webSocketConnectionJob?.takeIf { !it.isCancelled }?.cancel()
        webSocketConnectionJob = CoroutineScope(Dispatchers.IO).launch(handler) {
            startWebsocket()
        }
    }

    private fun isConnected() = session?.isActive == true

    fun observeEvent() = eventFlow.asSharedFlow()
}