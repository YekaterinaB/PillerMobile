package com.example.piller.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.models.CalendarProfile
import com.example.piller.models.Profile

class ProfileAdapter(
    private var _dataSet: MutableList<CalendarProfile>,
    private var _currentProfile: String,
    private val _clickOnItemListener: (Profile) -> Unit,
    private val _clickOnButtonListener: (Profile) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileName: TextView = view.findViewById(R.id.profile_name_profile_item)
        val relation: TextView = view.findViewById(R.id.profile_relation_profile_item)
        val deleteButton: ImageView = view.findViewById(R.id.trash_profile_item)
    }

    fun setData(data: MutableList<CalendarProfile>) {
        _dataSet = data
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.profile_item, viewGroup, false)

        return ViewHolder(view)
    }

    fun updateCurrentProfile(profile: String) {
        _currentProfile = profile
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val currentItem = _dataSet[position]
        val profileName = currentItem.getProfileName()
        viewHolder.profileName.text = profileName
        val profileRelation = currentItem.getProfileRelation()
        viewHolder.relation.text = profileRelation

        if (_currentProfile == profileName) {
            viewHolder.itemView.setBackgroundResource(R.drawable.rounded_shape_green_edge)
        } else {
            viewHolder.itemView.setBackgroundResource(R.drawable.rounded_shape_edit_text)
        }

        viewHolder.itemView.setOnClickListener {
            _clickOnItemListener(_dataSet[position].getProfileObject())
        }

        viewHolder.deleteButton.setOnClickListener { _clickOnButtonListener(_dataSet[position].getProfileObject()) }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = _dataSet.size
}