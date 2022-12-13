package ru.spbstu.king_game.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import ru.spbstu.king_game.R
import ru.spbstu.king_game.databinding.FragmentCurrentGameBinding
import ru.spbstu.king_game.engine.controller.GameStateController
import ru.spbstu.king_game.engine.data.GameState
import ru.spbstu.king_game.engine.repository.CurrentUserRepository
import ru.spbstu.king_game.navigation.Navigator

class CurrentGameFragment : Fragment() {

    private var _binding: FragmentCurrentGameBinding? = null
    private val binding get() = _binding!!

    private val currentUserRepository = CurrentUserRepository()
    private val gameStateController = GameStateController()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        binding.fieldView.currentUserRepository = currentUserRepository
        binding.fieldView.onCardSelected = {
            gameStateController.onCardSelected(it)
        }

        lifecycleScope.launchWhenStarted {
            gameStateController.fieldStateFlow.collect {
                binding.fieldView.fieldState = it
                if (it.deck.isNotEmpty()) {
                    binding.fieldView.updateState(it)
                }
                binding.fieldView.invalidate()
            }
        }
        lifecycleScope.launchWhenStarted {
            gameStateController.gameStateFlow.collect {
                when (it) {
                    GameState.Finished -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_over)
                            .show()
                    }
                    GameState.Paused -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_paused)
                            .show()
                    }
                    GameState.Prepared -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_prepared)
                            .show()
                    }
                    GameState.Started -> {
                        binding.fieldView.isVisible = true
                        binding.fieldView.invalidate()
                    }
                }
            }
        }
    }
}