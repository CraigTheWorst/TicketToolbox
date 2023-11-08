package com.example.tickettoolbox.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tickettoolbox.MainActivity
import com.example.tickettoolbox.R
import com.example.tickettoolbox.adapters.NoteAdapter
import com.example.tickettoolbox.data.NoteDAO
import com.example.tickettoolbox.data.NoteDBHelper
import com.example.tickettoolbox.data.NoteDC




class Notepad : AppCompatActivity() {

    private lateinit var newActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var notesHamburgerMenu: ImageButton
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var addNoteButton: ImageView
    private lateinit var btnDeleteAll: ImageButton
    private lateinit var btnFilter: ImageButton
    private lateinit var rootlayout: ConstraintLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var notes: MutableList<NoteDC>
    private lateinit var adapter: NoteAdapter
    private lateinit var noteDAO: NoteDAO


    private lateinit var drawerLayout: DrawerLayout
    private lateinit var noteTemplatesSpinner: Spinner
    private var isSpinnerInitialized = false
    private var lastSelectedPosition = -1

    private lateinit var Homedrawerbtn: TextView
    private lateinit var Formsdrawerbtn: TextView
    private lateinit var Imagesdrawerbtn: TextView
    private lateinit var ReturnPartsdrawerbtn: TextView

    private lateinit var gestureDetector: GestureDetectorCompat

    // Define the restaurant names array at the class level
    private val restaurantNames = arrayOf(
        "Select a template",
        "Applebee's",
        "Arby's",
        "Bonchon",
        "California Pizza Kitchen",
        "Denny's",
        "IHOP",
        "Jack in the Box",
        "Lovisa",
        "On The Border",
        "P.F. Chang's",
        "Panera Bread",
        "Steak N' Shake"
    )

    companion object {
        private const val REQUEST_CODE_EDIT_NOTE = 2
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notepad)


        // Initialize views, buttons, and spinners
        notesHamburgerMenu = findViewById(R.id.notesHamburgerMenu)
        noteRecyclerView = findViewById(R.id.noteRecyclerView)
        addNoteButton = findViewById(R.id.addNoteButton)
        rootlayout = findViewById(R.id.rootConstraintLayout)
        btnDeleteAll = findViewById(R.id.btn_deleteAll)
        btnFilter = findViewById(R.id.btnFilter)
        noteTemplatesSpinner = findViewById(R.id.Note_Templates_Spinner)

        // intent values
        val intentHome = Intent(this, MainActivity::class.java)
        val intentForms = Intent(this, Forms::class.java)
        val intentImages = Intent(this, Images::class.java)
        val intentReturnParts = Intent(this, Calendar::class.java)

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout_notepad)
        initializeHamburgerMenu()

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

        // Set the adapter for the Spinner using the class-level restaurantNames array
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, restaurantNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        noteTemplatesSpinner.adapter = spinnerAdapter

        noteTemplatesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isSpinnerInitialized) {
                    if (position != 0 && position != lastSelectedPosition) {
                        val selectedRestaurant = restaurantNames[position]
                        createNoteFromRestaurantTemplate(selectedRestaurant)
                    }
                } else {
                    // Spinner initialization, do nothing
                    isSpinnerInitialized = true
                }

                lastSelectedPosition = position

                // Set the spinner back to the "Select a template" position
                noteTemplatesSpinner.setSelection(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected
            }
        }





        // INITIALIZE CLICK LISTENERS
        Homedrawerbtn = findViewById(R.id.Home_Hamburger4)
        Homedrawerbtn.setOnClickListener { startActivity(intentHome)}

        Formsdrawerbtn = findViewById(R.id.Forms_Hamburger4)
        Formsdrawerbtn.setOnClickListener { startActivity(intentForms)}

        Imagesdrawerbtn = findViewById(R.id.Images_Hamburger4)
        Imagesdrawerbtn.setOnClickListener { startActivity(intentImages) }

        ReturnPartsdrawerbtn = findViewById(R.id.Return_Parts_Hamburger4)
        ReturnPartsdrawerbtn.setOnClickListener { startActivity(intentReturnParts) }
        // No action for the Return Form since it's the current activity



        // Initialize database and DAO
        val dbHelper = NoteDBHelper(this)
        noteDAO = NoteDAO(dbHelper)
        notes = mutableListOf()

        noteDAO.getAll()

        // Initialize the newActivityResultLauncher here, within the onCreate method
        newActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // The result has been received, now we need to refresh the RecyclerView
                // Fetch the updated list of notes from the database
                val updatedNotes = noteDAO.getAll()

                // Update the note list and notify the adapter
                notes.clear()
                notes.addAll(updatedNotes)
                adapter.notifyDataSetChanged()

                // Scroll to the top of the RecyclerView to display the newly added note
                noteRecyclerView.scrollToPosition(0)
            }
        }


        // Set up adapter for recycler view
        adapter = NoteAdapter(notes, object : NoteAdapter.OnItemClickListener {
            override fun onItemClick(note: NoteDC) {
                // Open EditNotes activity with the clicked note's data
                val intent = Intent(this@Notepad, EditNotes::class.java)
                intent.putExtra("noteId", note.id)
                intent.putExtra("noteTitle", note.title)
                intent.putExtra("noteContent", note.content)
                startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE)
            }

            override fun onItemLongClick(position: Int): Boolean {
                val note = adapter.getItemAt(position)
                showDeleteNoteDialog(note, position)
                return true // this will consume the long click event
            }

        })

        noteRecyclerView.adapter = adapter
        noteRecyclerView.layoutManager = GridLayoutManager(this, 3) // set the layout manager
        noteRecyclerView.addItemDecoration(NoteAdapter.ItemSpacingDecoration(12))

        // Load notes from database
        loadNotes()
        Log.d("MainActivity", "Loading notes...")
        sortDateModified()



        // Bottom Menu Bar OnClickListeners

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_notepad)
        swipeRefreshLayout.setOnRefreshListener {
            sortDateModified()
        }

        addNoteButton.setOnClickListener {
            val intent = Intent(this, EditNotes::class.java)
            newActivityResultLauncher.launch(intent)
        }

        btnDeleteAll.setOnClickListener {
            deleteAllNotes()
        }

        btnFilter.setOnClickListener {
            // Create a PopupMenu object anchored to the button
            val popup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PopupMenu(this, it, Gravity.END, 0, R.style.PopupMenuStyle)
            } else {
                PopupMenu(this, it, Gravity.END)
            }

            // Inflate the menu resource
            popup.menuInflater.inflate(R.menu.filter_menu, popup.menu)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                popup.menu.setGroupDividerEnabled(true) // Optional: Adds a divider between menu items
            }

            // Set a click listener for each menu item
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.note_filter_menu_sortDateMod -> {
                        // Perform action for option 1
                        sortDateModified()
                        true
                    }
                    R.id.note_filter_menu_sortDateCreated -> {
                        // Perform action for option 2
                        sortDateCreated()
                        true
                    }
                    R.id.note_filter_menu_sortAlpha -> {
                        // Perform action for option 3
                        sortaAlpha()
                        true
                    }
                    else -> false
                }
            }

            // Show the PopupMenu
            popup.show()
        }

        rootlayout.setOnClickListener {
            closeKeyboard()
        }

    }

    // Function to sort notes by date modified
    private fun sortDateModified() {
        notes.sortBy { it.modifiedTime }
        adapter.notifyDataSetChanged()
    }


    private fun sortDateCreated() {
        notes.sortBy { it.creationTime }
        adapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
    }

    // Function to sort notes alphabetically
    private fun sortaAlpha() {
        notes.sortBy { it.title }
        adapter.notifyDataSetChanged()
    }

    // Close Keyboard when touching random spot on screen
    private fun Activity.closeKeyboard() {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var currentFocus = this.currentFocus
        if (currentFocus == null) {
            currentFocus = View(this)
        }
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }

    private fun initializeHamburgerMenu() {
        notesHamburgerMenu = findViewById(R.id.notesHamburgerMenu)

        notesHamburgerMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }

    private fun loadNotes() {
        notes.clear()
        notes.addAll(noteDAO.getAll())
        adapter.notifyDataSetChanged()
        sortDateModified()
    }

    private fun saveNotes() {
        for (note in notes) {
            if (note.id == 0L) {
                // If the note is new, insert it into the database and update its ID
                val id = noteDAO.insertNote(note)
                note.id = id
            } else {
                // If the note already exists, update it in the database
                val updatedNote = NoteDC(note.id, note.title, note.content, System.currentTimeMillis())
                noteDAO.updateNote(updatedNote)
            }
        }

        adapter.notifyDataSetChanged()
    }


    override fun onPause() {
        super.onPause()
        saveNotes()
    }

    override fun onResume() {
        super.onResume()
        // Call a method to update your dataset and then notify the adapter.
        updateDataSet()
        adapter.notifyDataSetChanged()
        sortDateModified()

    }

    private fun updateDataSet() {
        // get all notes from database
        val updatedNotes = noteDAO.getAll()
        // Update list of notes
        adapter.updateNotes(updatedNotes)
    }


    private fun deleteAllNotes() {
        AlertDialog.Builder(this) // Assuming this code is within an Activity or Context-aware class
            .setTitle("Delete All Notes")
            .setMessage("Are you sure you want to delete all notes?")
            .setPositiveButton("Yes") { _, _ ->

                // Clear the notes from the list in memory
                notes.clear()

                // Delete all the notes from the database
                noteDAO.deleteAllNotes()

                // Optionally, you can schedule a layout animation
                noteRecyclerView.scheduleLayoutAnimation()

                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("No", null)  // Dismiss dialog if user chooses "No"
            .show()
    }



    private fun showDeleteNoteDialog(note: NoteDC, position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure you want to delete this note?")

        builder.setPositiveButton("Delete") { _, _ ->
            // Remove the note from the list and the database
            noteDAO.deleteNote(note)
            notes.removeAt(position)
            adapter.notifyDataSetChanged()          }

        builder.setNegativeButton("Cancel") { _, _ -> }

        builder.show()
    }




    private fun createNoteFromRestaurantTemplate(restaurantName: String) {
        val (templateTitle, templateContent) = when (restaurantName) {
            "Panera Bread" -> Pair(
                "Panera Template",
                """
            MOD:
            Panera HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "Applebee's" -> Pair(
                "Applebee's Template",
                """
            MOD:
            APB HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "On The Border" -> Pair(
                "On The Border Template",
                """
            MOD:        
            OTB HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "Bonchon" -> Pair(
                "Bonchon Template",
                """
            MOD:        
            Bonchon HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "Denny's" -> Pair(
                "Denny's Template",
                """
            MOD:        
            Denny's HD:
            Close-Out #: 
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "Steak N' Shake" -> Pair(
                "Steak N' Shake Template",
                """
            MOD:        
            SNS HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "P.F. Chang's" -> Pair(
                "P.F. Chang's Template",
                """
            MOD:        
            P.F. Chang's HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "Jack in the Box" -> Pair(
                "Jack in the Box Template",
                """
            MOD:        
            Jack HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "Lovisa" -> Pair(
                "Lovisa Template",
                """
            MOD:        
            Lovisa HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "California Pizza Kitchen" -> Pair(
                "CPK Template",
                """
            MOD:        
            CPK HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking:
            
            
            """.trimIndent()
            )
            "IHOP" -> Pair(
                "IHOP Template",
                """
            MOD:        
            IHOP HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking: 
            
            
            """.trimIndent()
            )
            "Arby's" -> Pair(
                "Arby's Template",
                """
            MOD:        
            Arby's HD:
            
            - No revisit needed
            - All work completed
            - No extra parts needed
            - No Return Shipping Label needed
            
            FDO:
            
            UPS Return Tracking: 
            
            
            """.trimIndent()
            )
            else -> Pair("Unknown", "No template available")
        }

        val templateNote = NoteDC(0L, templateTitle, templateContent, System.currentTimeMillis(), System.currentTimeMillis())

        // Add it to the database
        val id = noteDAO.insertNote(templateNote)
        templateNote.id = id

        // Add it to the in-memory list and refresh the RecyclerView
        notes.add(0, templateNote)
        adapter.notifyItemInserted(0)

        // Scroll to the top of the RecyclerView to display the newly added note
        noteRecyclerView.scrollToPosition(0)
    }








}