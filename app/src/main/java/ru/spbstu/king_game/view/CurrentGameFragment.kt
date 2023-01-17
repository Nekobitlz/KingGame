package ru.spbstu.king_game.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.vo.game.GameStateVO
import ru.spbstu.king_game.databinding.FragmentCurrentGameBinding
import ru.spbstu.king_game.engine.repository.DependencyProvider
import ru.spbstu.king_game.navigation.Navigator
import ru.spbstu.king_game.view.utils.BackHandler

class CurrentGameFragment : Fragment(), BackHandler {

    private var _binding: FragmentCurrentGameBinding? = null
    private val binding get() = _binding!!
    private val gameStateController get() = DependencyProvider.gameStateController!!

    private var pausedDialog: MaterialDialog? = null
    private var exitDialog: MaterialDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (DependencyProvider.gameStateController == null) {
            DependencyProvider.gameStateController = DependencyProvider.createGameStateController()
        }
        binding.btnExit.setOnClickListener {
            onExitClick()
        }
        binding.btnRules.setOnClickListener {
            Navigator.toRules()
        }
        binding.btnPause.setOnClickListener {
            gameStateController.onPauseRequest()
        }
        binding.fieldView.currentUserRepository = DependencyProvider.currentUserRepository
        binding.fieldView.onCardSelected = {
            gameStateController.onCardSelected(it)
        }

        lifecycleScope.launchWhenStarted {
            gameStateController.gameStateFlow.collect {
                pausedDialog?.dismiss()
                pausedDialog = null
                when (it) {
                    is GameStateVO.Finished -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_over)
                            .message(text = "Победитель: ${it.players[it.winner].name}")
                            .onCancel { activity?.onBackPressed() }
                            .show()
                        gameStateController.onGameClosed()
                        DependencyProvider.gameStateController = null
                    }
                    is GameStateVO.Paused -> {
                        pausedDialog = MaterialDialog(requireContext())
                            .title(R.string.game_paused)
                            .cancelOnTouchOutside(false)
                            .noAutoDismiss()
                            .negativeButton(text = "Продолжить") {
                                gameStateController.onResumeRequest()
                            }
                            .show { }
                    }
                    is GameStateVO.Cancelled -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_cancelled)
                            .onCancel { parentFragmentManager.popBackStack() }
                            .show()
                        gameStateController.onGameClosed()
                        DependencyProvider.gameStateController = null
                    }
                    is GameStateVO.Started -> {
                        binding.fieldView.updateState(it)
                    }
                    GameStateVO.NotInitialized -> {
                        pausedDialog = MaterialDialog(requireContext())
                            .title(R.string.game_prepared)
                            .noAutoDismiss()
                            .onCancel { onExitClick() }
                            .cancelOnTouchOutside(false)
                            .show { }
                    }
                    is GameStateVO.Error -> {
                        pausedDialog = MaterialDialog(requireContext())
                            .title(text = it.errorType.text)
                            .positiveButton(text = "Повторить") {
                                DependencyProvider.gameStateController?.onRetryRequest() ?: parentFragmentManager.popBackStack()
                            }
                            .onCancel { activity?.onBackPressed() }
                            .show { }
                        gameStateController.onGameClosed()
                        DependencyProvider.gameStateController = null
                    }
                }
                binding.fieldView.fieldState = it
                binding.fieldView.postInvalidate()
            }
        }
    }

    private fun onExitClick() {
        exitDialog = MaterialDialog(requireContext())
            .title(R.string.confirm_exit)
            .positiveButton(R.string.yes) {
                gameStateController.onGameClosed()
                DependencyProvider.gameStateController = null
                parentFragmentManager.popBackStack()
            }
            .negativeButton(R.string.no) {
                pausedDialog?.show()
            }
            .onCancel { pausedDialog?.show() }
            .show { }
    }

    override fun onBackPressed(): Boolean {
        if (parentFragmentManager.findFragmentByTag("RULES_FRAGMENT") != null) {
            return false
        }
        onExitClick()
        return true
    }
}