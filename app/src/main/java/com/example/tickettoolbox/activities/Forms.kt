package com.example.tickettoolbox.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tickettoolbox.R
import com.example.tickettoolbox.extensions.closeKeyboard
import com.example.tickettoolbox.databinding.ActivityFormsBinding
import com.example.tickettoolbox.extensions.calShow
import android.content.Intent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.tickettoolbox.MainActivity
import com.google.android.material.switchmaterial.SwitchMaterial


class Forms : AppCompatActivity() {

    private lateinit var binding: ActivityFormsBinding
    private lateinit var formsHamburgerMenu: ImageButton
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var Homedrawerbtn: TextView
    private lateinit var Notepaddrawerbtn: TextView
    private lateinit var Imagesdrawerbtn: TextView
    private lateinit var ReturnPartsdrawerbtn: TextView
    private lateinit var inventoryToggle: SwitchMaterial

    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent values
        val intentHome = Intent(this, MainActivity::class.java)
        val intentNotepad = Intent(this, Notepad::class.java)
        val intentImages = Intent(this, Images::class.java)
        val intentReturnParts = Intent(this, Calendar::class.java)


        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout_forms)

        // initialize hamburger menu
        formsHamburgerMenu = findViewById(R.id.formsHamburgerMenu)

        formsHamburgerMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }


        // Initialize GestureDetector to detect swipe gestures
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null && e2 != null) {
                    val deltaX = e2.x - e1.x
                    if (deltaX > 0) {
                        // Swipe from left to right (close the drawer)
                        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                            drawerLayout.closeDrawer(GravityCompat.END)
                            return true
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        // Set the drawer listener
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Handle drawer sliding
            }

            override fun onDrawerOpened(drawerView: View) {
                // Handle drawer opened
            }

            override fun onDrawerClosed(drawerView: View) {
                // Handle drawer closed
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Handle drawer state change
            }
        })


        // INITIALIZE CLICK LISTENERS
        Homedrawerbtn = findViewById(R.id.Home_Hamburger2)
        Homedrawerbtn.setOnClickListener { startActivity(intentHome)}

        Notepaddrawerbtn = findViewById(R.id.Notepad_Hamburger2)
        Notepaddrawerbtn.setOnClickListener { startActivity(intentNotepad) }

        Imagesdrawerbtn = findViewById(R.id.Images_Hamburger2)
        Imagesdrawerbtn.setOnClickListener { startActivity(intentImages) }

        ReturnPartsdrawerbtn = findViewById(R.id.Return_Parts_Hamburger2)
        ReturnPartsdrawerbtn.setOnClickListener { startActivity(intentReturnParts) }
        // No action for the Return Form since it's the current activity


        // Calendar onClickListener for the calendar
        binding.DateSelectionInput.setOnClickListener {
            calShow()
            // function found in 'extensions -> showCalendarForms.kt'
        }

        // Save Button OnClickListener
        binding.formSaveButton.setOnClickListener {
            if (insertData()) { // This will only be true if data is successfully inserted
                broadcastIntent()
                startActivity(intentReturnParts)
            }
        }

        // Initialize the Switch
        inventoryToggle = findViewById(R.id.inventorySwitchForms)
        // Initialize Serial Number Input visibility
        binding.SerialNumberInput.visibility = View.GONE
        // Set switch listener
        inventoryToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.SerialNumberInput.visibility = View.VISIBLE
            } else {
                binding.SerialNumberInput.visibility = View.GONE
            }
        }



        // Close Keyboard when open space is clicked
        binding.TextFieldConstraintContainer.setOnClickListener {
            closeKeyboard()
            // function found in 'extensions->closeKeyboard Ext.kt'
        }

    }



    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    // Function for inserting form data into sqlite Database
    private fun insertData(): Boolean {
        val db = this.openOrCreateDatabase("OldPartsData", MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS OldPartsData (id INTEGER PRIMARY KEY, techName VARCHAR, modName VARCHAR, ticketNum VARCHAR, trackingNum VARCHAR, serialNumber VARCHAR, partType VARCHAR, calendarDate VARCHAR);")


        // Setting Values and error conditions
        val etTechName = binding.TechNameEditText.text.toString().replace("'", "''")
        if (etTechName.isEmpty()) {
            binding.TechNameEditText.error = "Please enter a name"
            return false
        }

        val etMODName = binding.MODNameEditText.text.toString().replace("'", "''")
        if (etMODName.isEmpty()) {
            binding.MODNameEditText.error = "Please enter a name"
            return false
        }

        val etTicketNum = binding.TicketNumEditText.text.toString().replace("'", "''")
        if (etTicketNum.isEmpty()) {
            binding.TicketNumEditText.error = "Please enter a ticket number"
            return false
        }

        val etTrackingNum = binding.TrackingEditText.text.toString().replace("'", "''")
        if (etTrackingNum.isEmpty()) {
            binding.TrackingEditText.error = "Please enter a tracking number"
            return false
        }

        val etPartType = binding.PartTypeEditText.text.toString().replace("'", "''")
        if (etPartType.isEmpty()) {
            binding.PartTypeEditText.error = "Please enter a part type"
            return false
        }

        val etCalendar = binding.DateSelectionInput.text.toString().replace("'", "''")
        if (etCalendar.isEmpty()) {
            binding.DateSelectionInput.error = "Please enter a calendar date"
            return false
        }

        val etSerialNumber = if(binding.SerialNumberEditText.text.toString().isBlank()) "N/A" else binding.SerialNumberEditText.text.toString().replace("'", "''")
        if (etSerialNumber.isEmpty()) {
            binding.SerialNumberEditText.error = "Please enter a serial number"
            return false
        }

        val query = "INSERT INTO OldPartsData (techName, modName, ticketNum, trackingNum, partType, serialNumber, calendarDate) VALUES ('$etTechName', '$etMODName', '$etTicketNum', '$etTrackingNum', '$etPartType', '$etSerialNumber', '$etCalendar');"
        db.execSQL(query)
        Toast.makeText(this, "Data inserted successfully", Toast.LENGTH_LONG).show()
        binding.TechNameEditText.text?.clear()
        binding.MODNameEditText.text?.clear()
        binding.TicketNumEditText.text?.clear()
        binding.TrackingEditText.text?.clear()
        binding.PartTypeEditText.text?.clear()
        binding.SerialNumberEditText.text?.clear()
        binding.DateSelectionInput.text?.clear()
        db.close()
        return true

    }




    private fun broadcastIntent() {
        val intent = Intent("update_count")
        sendBroadcast(intent)
    }




}