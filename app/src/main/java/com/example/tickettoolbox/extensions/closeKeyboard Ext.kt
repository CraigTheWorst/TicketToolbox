package com.example.tickettoolbox.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

// Close Keyboard when touching random spot on screen
fun Activity.closeKeyboard() {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var currentFocus = this.currentFocus
    if (currentFocus == null) {
        currentFocus = View(this)
    }
    imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
}





