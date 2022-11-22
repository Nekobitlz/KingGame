package ru.spbstu.king_game.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.spbstu.king_game.R
import ru.spbstu.king_game.databinding.FragmentCurrentGameBinding
import ru.spbstu.king_game.engine.controller.GameStateController
import ru.spbstu.king_game.engine.data.GameState

class CurrentGameFragment : Fragment() {

    private var _binding: FragmentCurrentGameBinding? = null
    private val binding get() = _binding!!

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
        CoroutineScope(Dispatchers.Main).launch {
            gameStateController.stateSharedFlow.onEach {
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
            }.collect()
            gameStateController.fieldStateSharedFlow.onEach {
                binding.fieldView.fieldState = it
            }.collect()
        }
    }
}