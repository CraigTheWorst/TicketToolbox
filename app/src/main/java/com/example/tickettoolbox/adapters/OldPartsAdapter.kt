package com.example.tickettoolbox.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tickettoolbox.R
import com.example.tickettoolbox.models.OldPartsDataModel
import kotlinx.android.synthetic.main.cal_items.view.*

class OldPartsAdapter(private val oldPartsList: kotlin.collections.List<OldPartsDataModel>) : RecyclerView.Adapter<OldPartsAdapter.OldPartsViewHolder>() {

    private var oldPartsDataList = oldPartsList.toMutableList()

    // Function to show popup
    private fun showPopup(itemView: View, data: OldPartsDataModel) {
        val inflater = LayoutInflater.from(itemView.context)
        val popupView = inflater.inflate(R.layout.custom_popup, null)

        popupView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()  // Perform click action
                hideKeyboardCustomPopup(itemView.context, v)
            }
            true  // Return true so the touch event is consumed
        }

        val popup = AlertDialog.Builder(itemView.context)
            .setView(popupView)
            .create()

        val techNameEditText = popupView.findViewById<EditText>(R.id.techNameTextView)
        techNameEditText.setText(data.techName)

        val modNameEditText = popupView.findViewById<EditText>(R.id.modNameTextView)
        modNameEditText.setText(data.modName)

        val ticketNumberEditText = popupView.findViewById<EditText>(R.id.ticketNumberTextView)
        ticketNumberEditText.setText(data.ticketNum)

        val partTypeEditText = popupView.findViewById<EditText>(R.id.partTypeTextView)
        partTypeEditText.setText(data.partType)

        val trackingNumberEditText = popupView.findViewById<EditText>(R.id.trackingNumberEditText)
        trackingNumberEditText.setText(data.trackingNum)

        val serialNumberEditText = popupView.findViewById<EditText>(R.id.serialNumberEditText)
        serialNumberEditText.setText(data.serialNumber)

        val creationDateEditText = popupView.findViewById<TextView>(R.id.creationDateTextView)
        creationDateEditText.text = data.calendarDate


        val updateButton = popupView.findViewById<Button>(R.id.updateButton)
        updateButton.setOnClickListener {
            val updatedTechName = techNameEditText.text.toString()
            val updatedModName = modNameEditText.text.toString()
            val updatedTicketNum = ticketNumberEditText.text.toString()
            val updatedTrackingNum = trackingNumberEditText.text.toString()
            val updatedPartType = partTypeEditText.text.toString()
            val updatedSerialNumber = serialNumberEditText.text.toString()
            val updatedCreationDate = creationDateEditText.text.toString()

            // Database operation to update the record
            updateRecordInDb(itemView.context, data.myPackageId!!, updatedTechName, updatedModName, updatedTicketNum, updatedTrackingNum, updatedPartType, updatedSerialNumber, updatedCreationDate)

            // Fetch the updated data from the database
            val refreshedData = fetchDataFromDb(itemView.context)
            updateData(refreshedData)


            popup.dismiss()  // Close the dialog after updating
        }

        hideKeyboardCustomPopup(itemView.context, itemView)
        popup.show()
    }

    private fun updateRecordInDb(context: Context, packageId: String, techName: String, modName: String, ticketNum: String, trackingNum: String, partType: String, serialNumber: String, calendarDate: String) {
        val db = context.openOrCreateDatabase("OldPartsData", MODE_PRIVATE, null)

        val updateQuery = """
        UPDATE OldPartsData SET 
            techName = ?,
            modName = ?,
            ticketNum = ?,
            trackingNum = ?,
            partType = ?,
            serialNumber = ?,
            calendarDate = ?
        WHERE id = ?
    """
        db.execSQL(updateQuery, arrayOf(techName, modName, ticketNum, trackingNum, partType, serialNumber, calendarDate, packageId))
        db.close()
    }



    inner class OldPartsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(oldPartsDataModel: OldPartsDataModel) {
            itemView.tv_ticketNum.text = oldPartsDataModel.ticketNum
            itemView.tv_partType.text = oldPartsDataModel.partType
            itemView.tv_date.text = oldPartsDataModel.calendarDate

            // Set an onClickListener on the itemView to show popup when item is clicked
            itemView.setOnClickListener {
                showPopup(itemView, oldPartsDataModel)
            }

            itemView.setOnLongClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Get the position of the item in the RecyclerView
                        val position = adapterPosition

                        // Get the packageId of the item to delete
                        val packageId = oldPartsDataList[position].myPackageId

                        // Open the database
                        val db = itemView.context.openOrCreateDatabase("OldPartsData", MODE_PRIVATE, null)

                        // Delete the item from the database using the id column
                        db.execSQL("DELETE FROM OldPartsData WHERE id = '$packageId'")

                        // Close the database
                        db.close()

                        // Remove the item from the list of data models
                        oldPartsDataList.removeAt(position)

                        // Notify the adapter that the item has been removed
                        notifyItemRemoved(position)
                    }
                    .setNegativeButton("No", null)
                    .show()

                true // Indicate that the long click was handled
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OldPartsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cal_items, parent, false)
        return OldPartsViewHolder(view)
    }

    override fun onBindViewHolder(holder: OldPartsViewHolder, position: Int) {
        holder.bind(oldPartsDataList[position])
    }

    override fun getItemCount(): Int {
        return oldPartsDataList.size
    }

    fun updateData(newOldPartsDataList: kotlin.collections.List<OldPartsDataModel>) {
        Log.d("updateData", "Received list: $newOldPartsDataList")
        oldPartsDataList = newOldPartsDataList.toMutableList()
        notifyDataSetChanged()
    }

    fun deleteAllParts(context: Context) {
        // Prompt the user with a confirmation dialog
        AlertDialog.Builder(context)
            .setTitle("Delete All Items")
            .setMessage("Are you sure you want to delete all items?")
            .setPositiveButton("Yes") { _, _ ->
                // 1. Delete all records from the OldPartsData table in the database
                val db = context.openOrCreateDatabase("OldPartsData", MODE_PRIVATE, null)
                val deleteQuery = "DELETE FROM OldPartsData"
                db.execSQL(deleteQuery)
                db.close()

                // 2. Clear the oldPartsDataList
                oldPartsDataList.clear()

                // 3. Notify the adapter that the dataset has changed
                notifyDataSetChanged()
            }
            .setNegativeButton("No", null)  // Dismiss dialog if user chooses "No"
            .show()
    }




    fun filter(query: String) {
        oldPartsDataList = oldPartsList.filter { it.partType?.contains(query, ignoreCase = true) ?: false }.toMutableList()
        notifyDataSetChanged()
    }

    private fun hideKeyboardCustomPopup(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun fetchDataFromDb(context: Context): List<OldPartsDataModel> {
        val oldPartsDataModels = mutableListOf<OldPartsDataModel>()
        val db = context.openOrCreateDatabase("OldPartsData", MODE_PRIVATE, null)

        // Query to fetch data
        val fetchQuery = "SELECT * FROM OldPartsData"

        val cursor = db.rawQuery(fetchQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val packageIdIndex = cursor.getColumnIndex("id")
                val techNameIndex = cursor.getColumnIndex("techName")
                val modNameIndex = cursor.getColumnIndex("modName")
                val ticketNumIndex = cursor.getColumnIndex("ticketNum")
                val trackingNumIndex = cursor.getColumnIndex("trackingNum")
                val partTypeIndex = cursor.getColumnIndex("partType")
                val serialNumberIndex = cursor.getColumnIndex("serialNumber")
                val calendarDateIndex = cursor.getColumnIndex("calendarDate")

                if (packageIdIndex != -1 && techNameIndex != -1 && modNameIndex != -1 &&
                    ticketNumIndex != -1 && partTypeIndex != -1 && trackingNumIndex != -1 && serialNumberIndex != -1 && calendarDateIndex != -1) {

                    val packageId = cursor.getString(packageIdIndex)
                    val techName = cursor.getString(techNameIndex)
                    val modName = cursor.getString(modNameIndex)
                    val ticketNum = cursor.getString(ticketNumIndex)
                    val trackingNum = cursor.getString(trackingNumIndex)
                    val partType = cursor.getString(partTypeIndex)
                    val serialNumber = cursor.getString(serialNumberIndex)
                    val calendarDate = cursor.getString(calendarDateIndex)

                    val oldPartsDataModel = OldPartsDataModel(
                        myPackageId = packageId,
                        techName = techName,
                        modName = modName,
                        ticketNum = ticketNum,
                        trackingNum = trackingNum,
                        partType = partType,
                        serialNumber = serialNumber,
                        calendarDate = calendarDate
                    )

                    oldPartsDataModels.add(oldPartsDataModel)
                }

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return oldPartsDataModels
    }











}
