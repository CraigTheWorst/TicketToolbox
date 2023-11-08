package com.example.tickettoolbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.tickettoolbox.activities.Calendar
import com.example.tickettoolbox.activities.Images
import com.example.tickettoolbox.activities.Forms
import com.example.tickettoolbox.activities.Notepad
import com.example.tickettoolbox.data.NoteDBHelper
import com.example.tickettoolbox.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var Formsdrawerbtn: TextView
    private lateinit var Notepaddrawerbtn: TextView
    private lateinit var Imagesdrawerbtn: TextView
    private lateinit var ReturnPartsdrawerbtn: TextView

    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This line sets the app to always use light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // set values for intent for each button
        val intentForms = Intent(this, Forms::class.java)
        val intentNotepad = Intent(this, Notepad::class.java)
        val intentImages = Intent(this, Images::class.java)
        val intentReturnParts = Intent(this, Calendar::class.java)


        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout_main_activity)

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
        Formsdrawerbtn = findViewById(R.id.Forms_Hamburger3)
        Formsdrawerbtn.setOnClickListener { startActivity(intentForms)}

        Notepaddrawerbtn = findViewById(R.id.Notepad_Hamburger3)
        Notepaddrawerbtn.setOnClickListener { startActivity(intentNotepad) }

        Imagesdrawerbtn = findViewById(R.id.Images_Hamburger3)
        Imagesdrawerbtn.setOnClickListener { startActivity(intentImages) }

        ReturnPartsdrawerbtn = findViewById(R.id.Return_Parts_Hamburger3)
        ReturnPartsdrawerbtn.setOnClickListener { startActivity(intentReturnParts) }
        // No action for the Return Form since it's the current activity

        // This section contains all OnClickListeners for activity buttons

        binding.NotepadButton1.setOnClickListener {
            startActivity(intentNotepad)
        }

        binding.CalendarButton1.setOnClickListener {
            startActivity(intentReturnParts)
        }

        binding.FormsButton1.setOnClickListener {
            startActivity(intentForms)
        }

        binding.ImagesButton1.setOnClickListener {
            startActivity(intentImages)
        }

        // display total part numbers and click container to refresh.
        rpTotal()

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun rpTotal() {
        val count = rpCount()
        val notesCount = rpNotesCount()
        val imgCount = rpImgCount()
        binding.tvRPStats.text = count.toString()
        binding.tvRPStats2.text = imgCount.toString()
        binding.tvRPStats3.text = notesCount.toString()
    }

    private fun rpCount(): Int {
        val db = this.openOrCreateDatabase("OldPartsData", MODE_PRIVATE, null)
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='OldPartsData'", null)

        // If the table doesn't exist, return 0
        if (!cursor.moveToFirst()) {
            cursor.close()
            db.close()
            return 0
        }

        // Otherwise, proceed with the original logic
        val countCursor = db.rawQuery("SELECT * FROM OldPartsData", null)
        val count = countCursor.count
        countCursor.close()
        db.close()

        return count
    }


    private fun rpNotesCount(): Int {
        val dbHelper = NoteDBHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${NoteDBHelper.TABLE_NAME}", null)
        val count = cursor.count
        cursor.close()
        db.close()

        return count

    }

    private fun rpImgCount(): Int {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE 'Deliverable%'"
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        val count = cursor?.count ?: 0
        cursor?.close()

        return count
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "update_count") {
                rpTotal()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rpTotal()
        val rpFilter = IntentFilter("update_count")
        registerReceiver(receiver, rpFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }





}