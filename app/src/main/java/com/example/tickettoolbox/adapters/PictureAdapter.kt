package com.example.tickettoolbox.adapters

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tickettoolbox.R
import java.io.File

class PictureAdapter(
    internal var pictures: List<File>,
    private val onPictureClicked: (File) -> Unit,
    private val onPictureLongClicked: (File) -> Unit
) : RecyclerView.Adapter<PictureAdapter.PictureViewHolder>() {

    inner class PictureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val tvImgName: TextView = itemView.findViewById(R.id.tv_imgName)

        fun bind(picture: File) {
            Glide.with(itemView)
                .load(picture)
                .into(imageView)

            tvImgName.text = picture.name

            imageView.setOnClickListener {
                val uri = FileProvider.getUriForFile(itemView.context, "${itemView.context.applicationContext.packageName}.fileprovider", picture)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "image/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                itemView.context.startActivity(intent)

                onPictureClicked(picture)
            }

            imageView.setOnLongClickListener {
                val builder = AlertDialog.Builder(itemView.context)
                builder.setMessage("Are you sure you want to delete this picture?")
                    .setPositiveButton("Delete") { _, _ ->
                        deletePic(adapterPosition)
                        onPictureLongClicked(picture)
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                builder.show()
                true
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pic_outer_container_layout, parent, false)
        return PictureViewHolder(view)
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        holder.bind(pictures[position])
    }

    override fun getItemCount() = pictures.size

    fun setPictures(pictures: List<File>) {
        this.pictures = pictures
        notifyDataSetChanged()
    }

    fun deletePic(position: Int) {
        val picture = pictures[position]
        picture.delete()
        pictures = pictures.filter { it != picture }
        notifyItemRemoved(position)
    }

    // recyclerview item decoration and spacing
    class ItemSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.top = spacing
            outRect.bottom = spacing
            outRect.left = spacing
            outRect.right = spacing
        }
    }
}
