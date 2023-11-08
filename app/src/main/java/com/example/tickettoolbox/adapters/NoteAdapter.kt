package com.example.tickettoolbox.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.tickettoolbox.R
import com.example.tickettoolbox.data.NoteDC
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private var notes: MutableList<NoteDC>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(note: NoteDC)
        fun onItemLongClick(position: Int): Boolean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)

        // Set the click listener on the entire item view
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(note)
        }
        holder.itemView.setOnLongClickListener {
            onItemClickListener.onItemLongClick(position)
        }
    }



    override fun getItemCount(): Int {
        return notes.size
    }

    fun getItemAt(position: Int): NoteDC {
        return notes[position]
    }


    fun deleteItem(position: Int) {
        notes.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateNotes(newNotes: List<NoteDC>) {
        notes.clear() // notes is the list used by the adapter
        notes.addAll(newNotes)
    }


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleEditText: EditText = itemView.findViewById(R.id.noteTitleEditText)
        private val contentEditText: EditText = itemView.findViewById(R.id.noteContentEditText)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tv_dateTime)
        private val noteItemRelative: ConstraintLayout = itemView.findViewById(R.id.noteItemRelative)

        init {
            noteItemRelative.setOnClickListener {
                onItemClickListener.onItemClick(notes[adapterPosition])
            }
            noteItemRelative.setOnLongClickListener {
                onItemClickListener.onItemLongClick(adapterPosition)
            }
            titleEditText.setOnClickListener {
                onItemClickListener.onItemClick(notes[adapterPosition])
            }
            titleEditText.setOnLongClickListener {
                onItemClickListener.onItemLongClick(adapterPosition)
            }
            tvDateTime.setOnClickListener {
                onItemClickListener.onItemClick(notes[adapterPosition])
            }
            tvDateTime.setOnLongClickListener {
                onItemClickListener.onItemLongClick(adapterPosition)
            }
            contentEditText.setOnClickListener {
                onItemClickListener.onItemClick(notes[adapterPosition])
            }
            contentEditText.setOnLongClickListener {
                onItemClickListener.onItemLongClick(adapterPosition)
            }
        }

        fun bind(note: NoteDC) {
            titleEditText.setText(note.title)
            contentEditText.setText(note.content)
            tvDateTime.text = SimpleDateFormat("EEE, MMM d, yyyy hh:mm a").format(Date(note.creationTime))
        }
    }


    class ItemSpacingDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.bottom = spaceSize
            outRect.left = spaceSize
            outRect.right = spaceSize
            outRect.top = spaceSize
        }
    }

}
