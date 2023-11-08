package com.example.tickettoolbox.extensions

import android.app.Activity
import android.content.Intent
import com.example.tickettoolbox.MainActivity

fun Activity.homeBtn() {
    val intentHomeBtn = Intent(this, MainActivity::class.java)
    startActivity(intentHomeBtn)
}