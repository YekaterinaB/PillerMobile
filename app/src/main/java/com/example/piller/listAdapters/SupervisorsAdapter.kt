package com.example.piller.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.models.Supervisor

class SupervisorsAdapter(
    private var dataSet: MutableList<Supervisor>,
    private val clickOnDeleteButtonListener: (String) -> Unit,
    private val clickOnEditButtonListener: (String,String) -> Unit
) :
    RecyclerView.Adapter<SupervisorsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val supervisorName: TextView
        val supervisorEmail: TextView
        val editButton: ImageButton
        val deleteButton: ImageButton


        init {
            // Define click listener for the ViewHolder's View.
            supervisorName = view.findViewById(R.id.supervisor_name)
            supervisorEmail = view.findViewById(R.id.supervisor_email)
            editButton = view.findViewById(R.id.edit_supervisor_button)
            deleteButton = view.findViewById(R.id.delete_supervisor_button)
        }
    }

    fun setData(data: MutableList<Supervisor>) {
        dataSet = data
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.supervisor_recycleview_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val currentItem = dataSet[position]
        val email = currentItem.getsupervisorEmail()
        val name = currentItem.getSupervisorName()
        viewHolder.supervisorName.text = name
        viewHolder.supervisorEmail.text = email

        viewHolder.deleteButton.setOnClickListener { clickOnDeleteButtonListener(email) }
        viewHolder.editButton.setOnClickListener { clickOnEditButtonListener(name,email) }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}