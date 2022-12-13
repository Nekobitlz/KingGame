package ru.spbstu.king_game.view.features.rules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.spbstu.king_game.R
import ru.spbstu.king_game.databinding.FragmentRulesListBinding
import ru.spbstu.king_game.view.features.rules.data.RuleItem

class RulesFragment : Fragment() {

    private val rules = mapOf(
        R.string.rule_1_title to R.string.rule_1_description,
        R.string.rule_2_title to R.string.rule_2_description,
        R.string.rule_3_title to R.string.rule_3_description,
        R.string.rule_4_title to R.string.rule_4_description,
        R.string.rule_5_title to R.string.rule_5_description,
        R.string.rule_6_title to R.string.rule_6_description,
        R.string.rule_7_title to R.string.rule_7_description,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRulesListBinding.inflate(inflater, container, false)

        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            adapter = RuleItemAdapter(getRuleItems())
        }
        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        return binding.root
    }

    private fun getRuleItems() = rules.map {
        RuleItem(getString(it.key), getString(it.value))
    }
}