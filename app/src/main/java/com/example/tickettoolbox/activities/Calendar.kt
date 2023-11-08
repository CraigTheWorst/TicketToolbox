package com.example.tickettoolbox.activities



import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tickettoolbox.MainActivity
import com.example.tickettoolbox.R
import com.example.tickettoolbox.adapters.OldPartsAdapter
import com.example.tickettoolbox.databinding.ActivityCalendarBinding
import com.example.tickettoolbox.extensions.closeKeyboard
import com.example.tickettoolbox.models.OldPartsDataModel
import kotlinx.android.synthetic.main.activity_calendar.*
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Calendar : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var calHamburgerMenu: ImageButton
    private lateinit var deleteAllPartsButton: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshButton: ImageButton
    private lateinit var adapter: OldPartsAdapter
    private var listOfOldPartsData = listOf<OldPartsDataModel>()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var Homedrawerbtn: TextView
    private lateinit var Formsdrawerbtn: TextView
    private lateinit var Notepaddrawerbtn: TextView
    private lateinit var Imagesdrawerbtn: TextView

    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // intent values
        val intentHome = Intent(this, MainActivity::class.java)
        val intentNotepad = Intent(this, Notepad::class.java)
        val intentImages = Intent(this, Images::class.java)
        val intentForms = Intent(this, Forms::class.java)

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout_return_parts)


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


        // create OldPartsData Table
        createTable()

        // initializing UI components
        initializeHamburgerMenu()
        initializeRecyclerView()
        initializeRefreshButton()
        initializeCalendarViewButton()
        initializeAddItemButton()
        initializeDeleteAllPartsButton()
        initializeSearchView()



        // INITIALIZE OTHER CLICK LISTENERS
        Homedrawerbtn = findViewById(R.id.Home_Hamburger3)
        Homedrawerbtn.setOnClickListener { startActivity(intentHome)}

        Formsdrawerbtn = findViewById(R.id.Forms_Hamburger3)
        Formsdrawerbtn.setOnClickListener {startActivity(intentForms)}

        Notepaddrawerbtn = findViewById(R.id.Notepad_Hamburger3)
        Notepaddrawerbtn.setOnClickListener { startActivity(intentNotepad) }

        Imagesdrawerbtn = findViewById(R.id.Images_Hamburger3)
        Imagesdrawerbtn.setOnClickListener { startActivity(intentImages) }


        // No action for the Calendar/Return Parts Activity since it's the current activity

        // Close Keyboard when open space is clicked
        binding.calendarConstraint.setOnClickListener {
            closeKeyboard()
            // function found in 'extensions->closeKeyboard Ext.kt'
        }


    }


    private fun initializeHamburgerMenu() {
        calHamburgerMenu = findViewById(R.id.calHamburgerMenu)

        calHamburgerMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }


    private fun setOrUpdateAdapter(listOfData: List<OldPartsDataModel>) {
        if (::adapter.isInitialized) {
            adapter.updateData(listOfData)
        } else {
            adapter = OldPartsAdapter(listOfData)
            recyclerView.adapter = adapter
        }
    }


    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        CoroutineScope(Dispatchers.IO).launch {
            listOfOldPartsData = retrieveData()
            launch(Dispatchers.Main) {
                setOrUpdateAdapter(listOfOldPartsData)
            }
        }
    }

    private fun refreshRecyclerView() {

        CoroutineScope(Dispatchers.IO).launch {
            listOfOldPartsData = retrieveData()
            launch(Dispatchers.Main) {
                setOrUpdateAdapter(listOfOldPartsData)
            }
        }

    }

    private fun initializeRefreshButton() {
        refreshButton = findViewById(R.id.refresh_Button)
        binding.refreshButton.setOnClickListener {

            refreshRecyclerView()
            closeKeyboard()
        }
    }

    private fun initializeDeleteAllPartsButton() {
        deleteAllPartsButton = findViewById(R.id.btnDeleteAllParts)
        deleteAllPartsButton.setOnClickListener {
            adapter.deleteAllParts(this@Calendar)
        }
    }


    private fun initializeCalendarViewButton() {
        // CalendarView Button
        calendarViewButton.setOnClickListener {
            val c = Calendar.getInstance()
            val years = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = "${monthOfYear + 1}-$dayOfMonth-$year"
                listOfOldPartsData = retrieveData().filter { it.calendarDate?.let { calendar -> calendar == selectedDate } ?: false }
                runOnUiThread {
                    setOrUpdateAdapter(listOfOldPartsData)
                    Log.d("DatePickerDialog", "Selected date: $selectedDate")
                }
            }, years, month, day)
            dpd.show()
        }
    }

    private fun initializeAddItemButton() {
        binding.additemButton.setOnClickListener {
            val intentAddItemButton = Intent(this, Forms::class.java)
            startActivity(intentAddItemButton)
        }
    }

    private fun initializeSearchView() {
        // setting queryHint for the SearchView
        searchView = findViewById(R.id.sv_returnParts)
        if (searchView.visibility == View.VISIBLE && searchView.hasFocus()) {
            // SearchView is visible and has focus
            searchView.queryHint = "find old part or ticket #"
        } else {
            // SearchView is hidden or does not have focus
            searchView.queryHint = "find old part or ticket #"
        }


        // Add functionality to searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Filter the data based on the user's search query
                listOfOldPartsData = retrieveData(query)
                // Update the data in the RecyclerView adapter
                setOrUpdateAdapter(listOfOldPartsData)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter the data based on the user's search query
                listOfOldPartsData = retrieveData(newText)
                // Update the data in the RecyclerView adapter
                setOrUpdateAdapter(listOfOldPartsData)
                return true
            }
        })
    }

    private fun createTable() {
        val db = this.openOrCreateDatabase("OldPartsData", MODE_PRIVATE, null)
        val createTableQuery = """
        CREATE TABLE IF NOT EXISTS OldPartsData (
            id INTEGER PRIMARY KEY,
            techName VARCHAR,
            modName VARCHAR,
            ticketNum VARCHAR,
            trackingNum VARCHAR,
            partType VARCHAR,
            serialNumber VARCHAR,
            calendarDate VARCHAR
        )
    """.trimIndent()
        db.execSQL(createTableQuery)
        db.close()
    }

    private fun retrieveData(filter: String? = null): kotlin.collections.List<OldPartsDataModel> {
        val listOfOldPartsData = mutableListOf<OldPartsDataModel>()
        try {
            val db = this.openOrCreateDatabase("OldPartsData", MODE_PRIVATE, null)
            Log.d("retrieveData", "Opening database OldPartsData")

            // Using the table name and column names as per the insertion
            val query = "SELECT id, techName, modName, ticketNum, trackingNum, partType, serialNumber, calendarDate FROM OldPartsData"
            val cursor = db.rawQuery(query, null)
            Log.d("retrieveData", "Querying data from table OldPartsData")

            if (cursor.count == 0) {
                Log.d("retrieveData", "No records found")
            } else {
                while (cursor.moveToNext()) {
                    val idIndex = cursor.getColumnIndex("id")
                    val techNameIndex = cursor.getColumnIndex("techName")
                    val modNameIndex = cursor.getColumnIndex("modName")
                    val trackingNumIndex = cursor.getColumnIndex("trackingNum")
                    val ticketNumIndex = cursor.getColumnIndex("ticketNum")
                    val partTypeIndex = cursor.getColumnIndex("partType")
                    val serialNumberIndex = cursor.getColumnIndex("serialNumber")
                    val calendarDateIndex = cursor.getColumnIndex("calendarDate")

                    val id = cursor.getString(idIndex)
                    val techName = cursor.getString(techNameIndex)
                    val modName = cursor.getString(modNameIndex)
                    val trackingNum = cursor.getString(trackingNumIndex)
                    val ticketNum = cursor.getString(ticketNumIndex)
                    val partType = cursor.getString(partTypeIndex)
                    val serialNumber = cursor.getString(serialNumberIndex)
                    val calendarDate = cursor.getString(calendarDateIndex)



                    if (filter == null ||
                        techName.contains(filter, true) ||
                        modName.contains(filter, true) ||
                        ticketNum.contains(filter, true) ||
                        trackingNum.contains(filter, true) ||
                        partType.contains(filter, true) ||
                        serialNumber.contains(filter, true) ||
                        calendarDate.contains(filter, true)) {
                        listOfOldPartsData.add(OldPartsDataModel(id, techName, modName, ticketNum, trackingNum, partType, serialNumber, calendarDate))
                    }
                }
            }

            // close cursor and database
            cursor.close()
            Log.d("retrieveData", "Closing cursor")

            db.close()
            Log.d("retrieveData", "Closing database")
        } catch (e: Exception) {
            Log.e("retrieveData", "Error retrieving data", e)
        }

        // Log the values of the list
        Log.d("listOfOldPartsData", listOfOldPartsData.toString())

        return listOfOldPartsData
    }







}
