package ru.spbstu.king_game.network

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.*
import ru.spbstu.king_game.common.utils.Logger

class WebsocketService(
    private val client: HttpClient = NetworkProvider.client
) {

    private var session: DefaultClientWebSocketSession? = null
    private val sendCommandFlow = MutableSharedFlow<WebsocketRequest>(1)
    private val eventFlow = MutableSharedFlow<WebsocketResponse>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            startWebsocket()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun startWebsocket() {
        session = client.webSocketSession(NetworkProvider.WEBSOCKET_URL)
        val session = session!!
        Logger.i("WebSocket: started")
        session.closeReason.invokeOnCompletion {
            if (it is ClosedReceiveChannelException || it is CancellationException) {
                Logger.i("WebSocket: closed normally ${it.stackTraceToString()}")
                return@invokeOnCompletion
            }
            Logger.e("WebSocket: closed with exception ${it?.stackTraceToString()}")
        }
        session.incoming.consumeAsFlow().onEach { frame ->
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
                Logger.e("WebSocket: error while parse gameState, trying to parse session")
                val event = converter.deserialize<WebsocketResponse.OpenSession>(frame, charset)
                eventFlow.emit(event)
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

    fun sendCommand(command: WebsocketRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            sendCommandFlow.emit(command)
        }
    }

    fun observeEvent() = eventFlow.asSharedFlow()
}