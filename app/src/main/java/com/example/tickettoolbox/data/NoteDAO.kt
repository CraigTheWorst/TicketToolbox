package com.example.tickettoolbox.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoteDAO(databaseHelper: SQLiteOpenHelper) {

    companion object {
        private const val TABLE_NAME = "notes"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_CONTENT = "content"
        private const val COL_CREATION_TIME = "creation_time"
        private const val COL_MODIFIED_TIME = "modified_time"
    }

    private val db: SQLiteDatabase = databaseHelper.writableDatabase

    fun insertNote(note: NoteDC): Long {
        val values = ContentValues()
        values.put(COL_TITLE, note.title)
        values.put(COL_CONTENT, note.content)
        values.put(COL_CREATION_TIME, note.creationTime)
        values.put(COL_MODIFIED_TIME, note.modifiedTime)
        return db.insert(TABLE_NAME, null, values)

    }

    fun updateNote(note: NoteDC) {
        val values = ContentValues()
        values.put(COL_TITLE, note.title)
        values.put(COL_CONTENT, note.content)
        values.put(COL_MODIFIED_TIME, note.modifiedTime)
        val selection = "$COL_ID = ?"
        val selectionArgs = arrayOf(note.id.toString())
        db.update(TABLE_NAME, values, selection, selectionArgs)


    }

    fun deleteNote(note: NoteDC) {
        val selection = "$COL_ID = ?"
        val selectionArgs = arrayOf(note.id.toString())
        db.delete(TABLE_NAME, selection, selectionArgs)

    }

    fun deleteAllNotes() {
        db.delete(TABLE_NAME, null, null)
    }



    fun getAll(): List<NoteDC> {
        val notes = mutableListOf<NoteDC>()
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COL_MODIFIED_TIME DESC"
        )
        while (cursor.moveToNext()) {
            val note = mapCursorToNoteDC(cursor)
            notes.add(note)
        }
        cursor.close()
        return notes
    }

    fun getNoteById(id: Long): NoteDC? {
        val selection = "$COL_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
        return if (cursor.moveToFirst()) {
            mapCursorToNoteDC(cursor)
        } else {
            null
        }
    }

    private fun mapCursorToNoteDC(cursor: Cursor): NoteDC {
        val idIndex = cursor.getColumnIndex(COL_ID)
        val titleIndex = cursor.getColumnIndex(COL_TITLE)
        val contentIndex = cursor.getColumnIndex(COL_CONTENT)
        val creationTimeIndex = cursor.getColumnIndex(COL_CREATION_TIME)
        val modifiedTimeIndex = cursor.getColumnIndex(COL_MODIFIED_TIME)

        val id = if (idIndex >= 0) cursor.getLong(idIndex) else 0
        val title = if (titleIndex >= 0) cursor.getString(titleIndex) else ""
        val content = if (contentIndex >= 0) cursor.getString(contentIndex) else ""
        val creationTime = if (creationTimeIndex >= 0) cursor.getLong(creationTimeIndex) else 0
        val modifiedTime = if (modifiedTimeIndex >= 0) cursor.getLong(modifiedTimeIndex) else 0

        return NoteDC(id, title, content, creationTime, modifiedTime)
    }


}
