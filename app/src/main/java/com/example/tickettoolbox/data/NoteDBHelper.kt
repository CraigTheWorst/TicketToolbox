package com.example.tickettoolbox.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "notesdb.db"
private const val DATABASE_VERSION = 1

class NoteDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val TABLE_NAME = "notes"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_CREATION_TIME = "creation_time"
        const val COLUMN_MODIFIED_TIME = "modified_time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createNotesTableSql = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_CONTENT TEXT,
                $COLUMN_CREATION_TIME INTEGER,
                $COLUMN_MODIFIED_TIME INTEGER
            )
        """.trimIndent()

        db.execSQL(createNotesTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // no-op for now
    }

}
