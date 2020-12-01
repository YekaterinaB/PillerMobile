package com.example.piller.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.models.Profile

class ProfileAdapter(private var dataSet: MutableList<Profile>,
                     private val clickOnItemListener: (String) -> Unit,
                     private val clickOnButtonListener: (String)->Unit) :
    RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileName: TextView
        val deleteButton:ImageButton


        init {
            // Define click listener for the ViewHolder's View.
            profileName = view.findViewById(R.id.profile_name)
            deleteButton=view.findViewById(R.id.delete_profile_button)

        }
    }
    fun setData(data:MutableList<Profile>){
        dataSet=data
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.profile_recycleview_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val currentItem=dataSet[position]
        val profileName=currentItem.getProfileName()
        viewHolder.profileName.text = profileName
        viewHolder.itemView.setOnClickListener{clickOnItemListener(profileName)}

        // do not add main delete button
        if(position == 0){
            viewHolder.deleteButton.visibility = View.GONE
        }else{
            viewHolder.deleteButton.setOnClickListener{clickOnButtonListener(profileName)}
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}