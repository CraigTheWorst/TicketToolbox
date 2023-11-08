package com.example.tickettoolbox.extensions

import android.app.Activity
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.widget.EditText
import com.example.tickettoolbox.R


fun Activity.calShow() {
    val etCal = findViewById<EditText>(R.id.DateSelectionInput)
    val myCalendar = Calendar.getInstance()
    val years = myCalendar.get(Calendar.YEAR)
    val month = myCalendar.get(Calendar.MONTH)
    val day = myCalendar.get(Calendar.DAY_OF_MONTH)

    // Calendar Popup DatePickerDialog
    val calendarPopup = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->

        val months = monthOfYear + 1
        val date = (months.toString() + "-" + dayOfMonth + "-" + year)
        etCal.setText(date)

    },years,month,day
    )
    calendarPopup.show()
}