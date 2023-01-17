package ru.spbstu.king_game.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import ru.spbstu.king_game.MainActivity
import ru.spbstu.king_game.R
import ru.spbstu.king_game.view.CurrentGameFragment
import ru.spbstu.king_game.view.StartFragment
import ru.spbstu.king_game.view.features.rules.RulesFragment

object Navigator {
    var activity: MainActivity? = null

    fun toCurrentGame() {
        navigateTo(CurrentGameFragment(), addToBackstack = true)
    }

    fun toStart() {
        navigateTo(StartFragment())
    }

    fun toRules() {
        navigateTo(RulesFragment(), addToBackstack = true, add = true, tag = "RULES_FRAGMENT")
    }

    fun showSnackBar(view: View, text: String) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateTo(
        fragment: Fragment,
        args: Bundle? = null,
        addToBackstack: Boolean = false,
        popBackstack: Boolean = false,
        add: Boolean = false,
        tag: String? = null
    ) = activity?.let { activity ->
        fragment.arguments = args
        activity.supportFragmentManager.apply {
            if (popBackstack) popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            beginTransaction()
                .apply { if (add) add(R.id.container, fragment, tag) else replace(R.id.container, fragment, tag) }
                .apply { if (addToBackstack) addToBackStack(null) }
                .commit()
        }
    }
}