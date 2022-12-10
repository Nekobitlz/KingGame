package ru.spbstu.king_game.view.features.rules

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import ru.spbstu.king_game.databinding.ItemRuleBinding
import ru.spbstu.king_game.view.features.rules.data.RuleItem

class RuleItemAdapter(
    private val values: List<RuleItem>
) : RecyclerView.Adapter<RuleItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRuleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = values.size

    class ViewHolder(binding: ItemRuleBinding) : RecyclerView.ViewHolder(binding.root) {
        private val tvTitle: TextView = binding.tvTitle
        private val tvDescription: TextView = binding.tvDescription

        fun bind(item: RuleItem) {
            tvTitle.text = item.title
            tvDescription.text = item.description
        }
    }
}