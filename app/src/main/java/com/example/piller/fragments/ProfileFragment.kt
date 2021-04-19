package com.example.piller.fragments

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.listAdapters.ProfileAdapter
import com.example.piller.models.Profile
import com.example.piller.models.UserObject
import com.example.piller.viewModels.ProfileViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.profile_main_layout.*
import kotlinx.android.synthetic.main.profile_main_layout.view.*

class ProfileFragment : FragmentWithUserObject() {
    private val _viewModel: ProfileViewModel by activityViewModels()
    private lateinit var _profileAdapter: ProfileAdapter
    private lateinit var _profileRecycle: RecyclerView
    private lateinit var _fragmentView: View
    private lateinit var _dimLayout: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.profile_main_layout, container, false)
        initViews()
        setOnClickListeners()
        initRecyclersAndAdapters()
        initObservers()
        return _fragmentView
    }


    private fun initViews(){
        _dimLayout=_fragmentView.findViewById(R.id.profile_dim_layout)
        val mainProfileEmailTv=_fragmentView.findViewById<TextView>(R.id.email_main_profile)
        val mainProfileNameTv=_fragmentView.findViewById<TextView>(R.id.profile_name_title_item)
        mainProfileEmailTv.text= _loggedUserObject.email
        mainProfileNameTv.text= _loggedUserObject.mainProfile?.name ?: "Main User"
    }

    private fun initObservers() {
        _viewModel.mutableToastError.observe(
            viewLifecycleOwner,
            Observer { toastMessage ->
                toastMessage?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        SnackBar.showToastBar(this.requireContext(), toastMessage)
                    }
                }
            })

        _viewModel.mutableListOfProfiles.observe(
            viewLifecycleOwner,
            Observer { profileList ->
                profileList?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        updateRecyclersAndAdapters()
                    }
                }
            })
    }


    private fun updateRecyclersAndAdapters() {
        _profileAdapter.setData(_viewModel.getListOfSecondaryProfiles())
        _profileAdapter.notifyDataSetChanged()
    }

    private fun initRecyclersAndAdapters() {
        _profileRecycle = _fragmentView.findViewById(R.id.profile_list_of_items)
        // initiate list of profiles with recyclers and adapters
        val profileList = _viewModel.getListOfSecondaryProfiles()
        _profileRecycle.layoutManager = LinearLayoutManager(_fragmentView.context)
        _profileAdapter = ProfileAdapter(
            profileList,
            clickOnItemListener = { changeProfile(it) },
            clickOnButtonListener = { removeProfilePopup(it) })
        _profileRecycle.adapter = _profileAdapter
    }

    private fun setOnClickListeners() {
        _fragmentView.add_new_profile_tx.setOnClickListener{
            showAddProfileToUserWindow()
        }

        _fragmentView.profile_title_item.setOnClickListener{
            changeProfile(_loggedUserObject.mainProfile!!)
        }
    }

    private fun changeProfile(profile: Profile) {
        _viewModel.setCurrentProfile(profile)
    }

    private fun removeProfilePopup(profile: Profile) {
        val customView: View = layoutInflater.inflate(R.layout.profile_remove_popup, null)
        val popup = PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            popup.setElevation(5.0f);
        }

        val cancelViewText = customView.findViewById<TextView>(R.id.cancel_remove_profile_popup)
        val removeViewText = customView.findViewById<TextView>(R.id.remove_profile_remove_profile_popup)
        val nameViewText = customView.findViewById<TextView>(R.id.profile_name_remove_popup)
        nameViewText.text = profile.name + " will be removed from your\nprofile list."
        cancelViewText.setOnClickListener {
            popup.dismiss()
            changeDarkBackgroundVisibility(false)
        }

        removeViewText.setOnClickListener {
            _viewModel.deleteOneProfile(_loggedUserObject.userId,profile)
            popup.dismiss()
            changeDarkBackgroundVisibility(false)

        }
        changeDarkBackgroundVisibility(true)
        popup.showAtLocation(_fragmentView, Gravity.CENTER, 0, 0)
    }


    private fun changeDarkBackgroundVisibility(isVisible:Boolean){
        if(isVisible){
            _dimLayout.visibility=View.VISIBLE

        }else{
            _dimLayout.visibility=View.GONE
        }
    }

    private fun showAddProfileToUserWindow() {
        val itemView = LayoutInflater.from(this.context)
            .inflate(R.layout.add_profile_layout, null)

        // create pop up window for add profile
        MaterialStyledDialog.Builder(this.context)
            .setIcon(R.drawable.ic_profile_blue)
            .setTitle("ADD A NEW PROFILE")
            .setCustomView(itemView)
            .setNegativeText("CANCEL")
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveText("ADD PROFILE")
            .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                val profileName = itemView.findViewById<View>(R.id.profile_name) as MaterialEditText
                val profileRelation = itemView.findViewById<View>(R.id.profile_relation) as MaterialEditText

                when {
                    TextUtils.isEmpty(profileName.text.toString()) -> {
                        SnackBar.showToastBar(
                            this.context,
                            "Profile name cannot be empty"
                        )
                        return@SingleButtonCallback
                    }
                }
                _viewModel.addProfileToDB(profileName.text.toString(), _loggedUserObject,profileRelation.text.toString())
            })
            .build()
            .show()
    }


    companion object {
        fun newInstance(loggedUser: UserObject) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    _loggedUserObject = loggedUser
                }
            }
    }
}