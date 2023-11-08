package com.example.tickettoolbox.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tickettoolbox.adapters.NoteAdapter
import com.example.tickettoolbox.data.NoteDAO
import com.example.tickettoolbox.data.NoteDBHelper
import com.example.tickettoolbox.data.NoteDC
import com.example.tickettoolbox.R
import kotlinx.android.synthetic.main.activity_edit_notes.*

class EditNotes : AppCompatActivity() {

    private lateinit var adapter: NoteAdapter
    private lateinit var noteDialogConstLayout: ConstraintLayout
    private lateinit var etTitle2: EditText
    private lateinit var etContent2: EditText
    private lateinit var saveButton: ImageButton
    private lateinit var noteDAO: NoteDAO
    private var note: NoteDC? = null
    private var noteId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_notes)

        noteDialogConstLayout = findViewById(R.id.noteDialogConstLayout)

        etTitle2 = findViewById(R.id.noteTitleEditText2)
        etContent2 = findViewById(R.id.noteContentEditText2)
        saveButton = findViewById(R.id.btn_editNotesBackNav)

        noteDAO = NoteDAO(NoteDBHelper(this))


        val noteId = intent.getLongExtra("noteId", -1)
        val noteTitle = intent.getStringExtra("noteTitle")
        val noteContent = intent.getStringExtra("noteContent")

        if (noteId != -1L) {
            // Load existing note details
            note = NoteDC(noteId, noteTitle.toString(), noteContent.toString(), System.currentTimeMillis(), System.currentTimeMillis())
            etTitle2.setText(note?.title)
            etContent2.setText(note?.content)
        }

        val touchListener = View.OnTouchListener { _, event ->
            // Check if touch event is below the tv_dateTime
            if (event.y > tv_dateTime.bottom) {
                // Request focus on the content EditText
                etContent2.requestFocus()
                // Open the keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etContent2, InputMethodManager.SHOW_IMPLICIT)
                // Place cursor at the end
                etContent2.setSelection(etContent2.text.length)
            }
            true
        }

// Set the touch listener on the parent layout
        noteDialogConstLayout.setOnTouchListener(touchListener)

        saveButton.setOnClickListener {
            saveNote()
        }


    }


    override fun onBackPressed() {
        saveNote()
        super.onBackPressed()
    }


    private fun saveNote() {
        val title = etTitle2.text.toString().trim()
        val content = etContent2.text.toString().trim()

        // Check if both title and content are empty
        if (title.isEmpty() && content.isEmpty()) {
            // If both are empty, we simply finish the activity without saving
            finish()
            return
        }

        if (note != null) {
            note!!.title = title
            note!!.content = content
            noteDAO.updateNote(note!!)
        } else {
            // Insert new note if it doesn't exist
            val newNote = NoteDC(title = title, content = content)
            noteDAO.insertNote(newNote)
        }

        // Set the result
        val resultIntent = Intent().apply {
            putExtra("noteTitle", title)
            putExtra("noteContent", content)
            putExtra("noteEdited", true)
        }
        setResult(Activity.RESULT_OK, resultIntent)

        finish()
    }


}
