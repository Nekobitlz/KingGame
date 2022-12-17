package ru.spbstu.king_game.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.vo.game.GameStateVO
import ru.spbstu.king_game.databinding.FragmentCurrentGameBinding
import ru.spbstu.king_game.engine.repository.DependencyProvider
import ru.spbstu.king_game.navigation.Navigator

class CurrentGameFragment : Fragment() {

    private var _binding: FragmentCurrentGameBinding? = null
    private val binding get() = _binding!!

    private val gameStateController = DependencyProvider.gameStateController

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
        binding.fieldView.currentUserRepository = DependencyProvider.currentUserRepository
        binding.fieldView.onCardSelected = {
            gameStateController.onCardSelected(it)
        }

        lifecycleScope.launchWhenStarted {
            gameStateController.gameStateFlow.collect {
                when (it) {
                    is GameStateVO.Finished -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_over)
                            .show()
                    }
                    is GameStateVO.Paused -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_paused)
                            .show()
                    }
                    is GameStateVO.Cancelled -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_cancelled)
                            .show()
                    }
                    is GameStateVO.Started -> {
                        binding.fieldView.updateState(it)
                    }
                    GameStateVO.NotInitialized -> {
                        MaterialDialog(requireContext())
                            .title(R.string.game_prepared)
                            .show()
                    }
                }
                binding.fieldView.fieldState = it
                binding.fieldView.invalidate()
            }
        }
    }
}