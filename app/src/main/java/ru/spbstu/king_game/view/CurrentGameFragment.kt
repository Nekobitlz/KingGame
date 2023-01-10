package ru.spbstu.king_game.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.vo.game.GameStateVO
import ru.spbstu.king_game.databinding.FragmentCurrentGameBinding
import ru.spbstu.king_game.engine.repository.DependencyProvider
import ru.spbstu.king_game.navigation.Navigator

class CurrentGameFragment : Fragment() {

    private var _binding: FragmentCurrentGameBinding? = null
    private val binding get() = _binding!!
    private val gameStateController get() = DependencyProvider.gameStateController!!

    private var pausedDialog: MaterialDialog? = null

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
            MaterialDialog(requireContext())
                .title(R.string.confirm_exit)
                .positiveButton(R.string.yes) {
                    requireActivity().onBackPressed()
                }
                .negativeButton(R.string.no)
                .show()
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
                when (it) {
                    is GameStateVO.Finished -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_over)
                            .message(text = "Победитель: ${it.players[it.winner].name}")
                            .onDismiss { activity?.onBackPressed() }
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
                            .onDismiss { activity?.onBackPressed() }
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
                            .cancelOnTouchOutside(false)
                            .show { }
                    }
                }
                binding.fieldView.fieldState = it
                binding.fieldView.postInvalidate()
            }
        }
    }
}