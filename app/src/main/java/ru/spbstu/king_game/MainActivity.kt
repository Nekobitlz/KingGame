package ru.spbstu.king_game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.spbstu.king_game.navigation.Navigator
import ru.spbstu.king_game.view.utils.BackHandler

//https://github.com/EXL/Snooder21/blob/master/snooder21/src/main/java/ru/exlmoto/snood21/SnoodsSurfaceView.java
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // todo inject in Application file
        Navigator.activity = this

        if (savedInstanceState == null) {
            // todo check isAuthorized)
            Navigator.toStart()
        }
    }

    override fun onBackPressed() {
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is BackHandler && fragment.onBackPressed()) {
                return
            }
        }
        super.onBackPressed()
    }
}