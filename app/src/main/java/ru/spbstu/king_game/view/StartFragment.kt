package ru.spbstu.king_game.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.spbstu.king_game.databinding.FragmentStartBinding
import ru.spbstu.king_game.engine.repository.DependencyProvider
import ru.spbstu.king_game.navigation.Navigator

class StartFragment : Fragment() {

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStart.setOnClickListener {
            if (binding.etName.text.isNullOrBlank()) {
                binding.nameField.isErrorEnabled = true
                binding.nameField.error = "Для начала игры необходимо ввести имя"
                return@setOnClickListener
            }
            DependencyProvider.currentUserRepository.currentName = binding.etName.text.toString().trim()
            Navigator.toCurrentGame()
        }
        DependencyProvider.currentUserRepository.currentName?.let {
            if (it.isNotBlank()) {
                binding.etName.setText(it)
            }
        }
    }
}