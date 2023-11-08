package com.example.tickettoolbox.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tickettoolbox.MainActivity
import com.example.tickettoolbox.R
import com.example.tickettoolbox.adapters.PictureAdapter
import kotlinx.coroutines.*
import java.io.File

class Images : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    private lateinit var imagesHamburgerMenu: ImageButton
    private lateinit var pictureRecyclerView: RecyclerView
    private lateinit var pictureAdapter: PictureAdapter
    private lateinit var btnDeleteAll: ImageButton
    private lateinit var picBtnFilter: ImageButton


    private lateinit var drawerLayout: DrawerLayout
    private lateinit var Homedrawerbtn: TextView
    private lateinit var Formsdrawerbtn: TextView
    private lateinit var Notepaddrawerbtn: TextView
    private lateinit var ReturnPartsdrawerbtn: TextView

    private lateinit var gestureDetector: GestureDetectorCompat

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)


        // set values for intent for each button
        val intentHome = Intent(this, MainActivity::class.java)
        val intentForms = Intent(this, Forms::class.java)
        val intentNotepad = Intent(this, Notepad::class.java)
        val intentReturnParts = Intent(this, Calendar::class.java)

        // Initialize Hamburger Menu
        initializeHamburgerMenu()

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout_images_activity)

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
        Formsdrawerbtn = findViewById(R.id.Forms_Hamburger5)
        Formsdrawerbtn.setOnClickListener { startActivity(intentForms)}

        Notepaddrawerbtn = findViewById(R.id.Notepad_Hamburger5)
        Notepaddrawerbtn.setOnClickListener { startActivity(intentNotepad) }

        Homedrawerbtn = findViewById(R.id.Home_Hamburger5)
        Homedrawerbtn.setOnClickListener { startActivity(intentHome)}

        ReturnPartsdrawerbtn = findViewById(R.id.Return_Parts_Hamburger5)
        ReturnPartsdrawerbtn.setOnClickListener { startActivity(intentReturnParts) }
        // No action for the Images activity since it's the current activity

        pictureAdapter = PictureAdapter(mutableListOf(),
            onPictureClicked = {},
            onPictureLongClicked = { picture ->
                // Picture deleted, update the list
                val pictures = pictureAdapter.pictures.toMutableList()
                pictures.remove(picture)
                pictureAdapter.setPictures(pictures)
            })

        // initialize the recycler view, the 'delete all' button, and the filter button
        pictureRecyclerView = findViewById(R.id.picRecyclerView)
        btnDeleteAll = findViewById(R.id.picbtn_deleteAll)
        picBtnFilter = findViewById(R.id.picbtnFilter)

        // load the picture items into the recycler view (update adapter data set)
        loadPictures()

        // call this method after you have updated your adapter data set
        pictureRecyclerView.adapter?.notifyDataSetChanged()

        // invalidate the RecyclerView to refresh the UI
        pictureRecyclerView.invalidate()

        // apply grid layout with 3 to a row
        pictureRecyclerView.apply {
            layoutManager = GridLayoutManager(this@Images, 3)
            adapter = pictureAdapter
            addItemDecoration(PictureAdapter.ItemSpacingDecoration(8))
        }

        // 'Delete All' button
        btnDeleteAll.setOnClickListener {
            deleteAllPics()
        }

        picBtnFilter.setOnClickListener {
            // Create a PopupMenu object anchored to the button
            // Set the popup menu style for API >= M
            val popup = PopupMenu(picBtnFilter.context, picBtnFilter, Gravity.END, 0, R.style.PopupMenuStyle)

            // Inflate the menu resource
            popup.menuInflater.inflate(R.menu.pic_filter_menu, popup.menu)

            // Set a click listener for each menu item
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.pic_filter_menu_sortDateMod -> {
                        // Perform action for option 1
                        sortDateModified()
                        true
                    }
                    R.id.pic_filter_menu_sortDateCreated -> {
                        // Perform action for option 2
                        sortDateCreated()
                        true
                    }

                    R.id.pic_refresh -> {
                        // Perform action for option 3
                        loadPictures()
                        true
                    }

                    else -> false
                }
            }

            // Show the PopupMenu
            popup.show()
        }


    }

    private fun initializeHamburgerMenu() {
        imagesHamburgerMenu = findViewById(R.id.imagesHamburgerMenu)

        imagesHamburgerMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun loadPictures() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            return
        }

        val pictures = mutableListOf<File>()

        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE '%Deliverable%'"
        val selectionArgs = null
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (it.moveToNext()) {
                val filePath = it.getString(columnIndex)
                pictures.add(File(filePath))
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            val loadedPictures = mutableListOf<Bitmap>()

            withContext(Dispatchers.IO) {
                for (file in pictures) {
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 8
                    }
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                    loadedPictures.add(bitmap)
                }
            }

            pictureAdapter.setPictures(pictures)
        }
    }



    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // One permission granted, check if another permission is needed and request it
                if (permissions.size > 1 && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission()
                } else {
                    // Both permissions granted or only one permission needed, load pictures
                    loadPictures()
                }
            } else {
                // Permission denied, show a toast message or handle it gracefully
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // User has denied the permission previously, explain why the permission is needed
                    Toast.makeText(this, "This permission is required to delete pictures.", Toast.LENGTH_SHORT).show()
                    requestStoragePermission()
                } else {
                    // User has denied the permission with 'Never ask again', show a dialog to direct the user to app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                    Toast.makeText(this, "Please grant the required permissions from settings.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    private fun deleteAllPics() {
        AlertDialog.Builder(this) // Assuming this code is within an Activity or Context-aware class
            .setTitle("Delete All Pictures")
            .setMessage("Are you sure you want to delete all pictures?")
            .setPositiveButton("Yes") { _, _ ->

                val pictures = mutableListOf<File>()

                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE '%Deliverable%'"
                val selectionArgs = null
                val sortOrder = null

                val cursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                cursor?.use {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                    while (it.moveToNext()) {
                        val filePath = it.getString(columnIndex)
                        pictures.add(File(filePath))
                    }
                }

                for (file in pictures) {
                    if (file.exists()) {
                        file.delete()
                    }
                }

                // Update the list after deleting the pictures
                loadPictures()
            }
            .setNegativeButton("No", null)  // Dismiss dialog if user chooses "No"
            .show()
    }




    private fun sortDateModified() {
        val sortedPictures = pictureAdapter.pictures.sortedByDescending { it.lastModified() }
        pictureAdapter.setPictures(sortedPictures)
    }

    private fun sortDateCreated() {
        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE '%Deliverable%'"
        val selectionArgs = null
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val pictures = mutableListOf<File>()

            val columnIndexData = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val columnIndexDateAdded = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val filePath = it.getString(columnIndexData)
                val dateAdded = it.getLong(columnIndexDateAdded)
                pictures.add(File(filePath).apply {
                    setLastModified(dateAdded * 1000) // set last modified date to created date
                })
            }

            pictureAdapter.setPictures(pictures)
        }
    }



}