package ru.spbstu.king_game.common.utils

import android.util.Log

object Logger {

    private val TAG = "KingGameTag"

    fun i(text: String) = Log.i(TAG, text)
    fun d(text: String) = Log.d(TAG, text)
    fun w(text: String) = Log.w(TAG, text)
    fun e(text: String) = Log.e(TAG, text)
    fun e(text: String, throwable: Throwable) = Log.e(TAG, text, throwable)
}