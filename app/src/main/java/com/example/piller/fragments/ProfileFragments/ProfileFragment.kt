package com.example.piller.fragments.ProfileFragments

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.activities.MainActivity
import com.example.piller.fragments.FragmentWithUserObject
import com.example.piller.listAdapters.ProfileAdapter
import com.example.piller.models.Profile
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.ProfileViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.profile_main_layout.*
import kotlinx.android.synthetic.main.profile_main_layout.view.*
import kotlinx.android.synthetic.main.supervisor_title_item.view.*

class ProfileFragment : FragmentWithUserObject() {
    private val _viewModel: ProfileViewModel by activityViewModels()
    private lateinit var _profileAdapter: ProfileAdapter
    private lateinit var _profileRecycle: RecyclerView
    private lateinit var _fragmentView: View
    private lateinit var _dimLayout: RelativeLayout
    private lateinit var _settingsImage: ImageView
    private lateinit var _supervisorTitle: ConstraintLayout
    private lateinit var _addNewProfile: TextView
    private lateinit var _profileTitle: ConstraintLayout

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
        updateCurrentProfile(loggedUserObject.currentProfile!!)
        (activity as MainActivity).setCurrentProfileTvVisibility(true)
        return _fragmentView
    }


    private fun initViews() {
        _dimLayout = _fragmentView.findViewById(R.id.profile_dim_layout)
        val mainProfileEmailTv = _fragmentView.findViewById<TextView>(R.id.email_main_profile)
        val mainProfileNameTv = _fragmentView.findViewById<TextView>(R.id.profile_name_title_item)
        mainProfileEmailTv.text = loggedUserObject.email
        mainProfileNameTv.text =
            loggedUserObject.mainProfile?.name ?: getString(R.string.mainSpaceUser)
        //remove number of supervisors
        _fragmentView.number_of_supervisors_title_item.visibility = View.INVISIBLE
        _settingsImage = _fragmentView.findViewById(R.id.settings_image_view)
        _supervisorTitle = _fragmentView.findViewById(R.id.supervisor_title_item_in_profiles)
        _addNewProfile = _fragmentView.findViewById(R.id.add_new_profile_tx)
        _profileTitle = _fragmentView.findViewById(R.id.profile_title_item)

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
            loggedUserObject.currentProfile!!.name,
            _clickOnItemListener = { updateCurrentProfile(it) },
            _clickOnButtonListener = { removeProfilePopup(it) })
        _profileRecycle.adapter = _profileAdapter
    }


    private fun setOnClickListeners() {
        _supervisorTitle.setOnClickListener {
            switchFragment(
                SupervisorsFragment.newInstance(loggedUserObject),
                DbConstants.SUPERVISOR_FRAGMENT_ID
            )
        }

        _settingsImage.setOnClickListener {
            switchFragment(
                SettingsFragment.newInstance(loggedUserObject),
                DbConstants.SETTINGS_FRAGMENT_ID
            )
        }

        _addNewProfile.setOnClickListener {
            showAddProfileToUserWindow()
        }

        _profileTitle.setOnClickListener {
            updateCurrentProfile(loggedUserObject.mainProfile!!)
        }
    }

    private fun switchFragment(fragment: Fragment, fragmentId: String) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(
                R.id.calender_weekly_container_fragment, fragment, fragmentId
            )
            transaction.addToBackStack(fragmentId)
            transaction.commit()
        }
    }

    private fun updateCurrentProfile(profile: Profile) {
        _viewModel.setCurrentProfile(profile)
        if (profile.name == loggedUserObject.mainProfile!!.name) {
            _profileTitle.setBackgroundResource(R.drawable.rounded_shape_green_edge)
        } else {
            _profileTitle.setBackgroundResource(R.drawable.rounded_shape_edit_text)

        }
        _profileAdapter.updateCurrentProfile(profile.name)
        _profileAdapter.notifyDataSetChanged()
    }


    private fun removeProfilePopup(profile: Profile) {
        val customView: View = layoutInflater.inflate(R.layout.profile_remove_popup, null)
        val popup = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )


        val cancelViewText = customView.findViewById<TextView>(R.id.cancel_remove_profile_popup)
        val removeViewText =
            customView.findViewById<TextView>(R.id.remove_profile_remove_profile_popup)
        val nameViewText = customView.findViewById<TextView>(R.id.profile_name_remove_popup)
        nameViewText.text = profile.name + " will be removed from your\nprofile list."
        cancelViewText.setOnClickListener {
            popup.dismiss()
            changeDarkBackgroundVisibility(false)
        }

        removeViewText.setOnClickListener {
            _viewModel.deleteOneProfile(loggedUserObject.userId, profile)
            updateCurrentProfile(loggedUserObject.mainProfile!!)
            popup.dismiss()
            changeDarkBackgroundVisibility(false)

        }
        changeDarkBackgroundVisibility(true)
        popup.showAtLocation(
            _fragmentView,
            Gravity.CENTER,
            DbConstants.popupX,
            DbConstants.popupY
        )
    }


    private fun changeDarkBackgroundVisibility(isVisible: Boolean) {
        if (isVisible) {
            _dimLayout.visibility = View.VISIBLE

        } else {
            _dimLayout.visibility = View.GONE
        }
    }

    private fun showAddProfileToUserWindow() {
        val itemView = LayoutInflater.from(this.context)
            .inflate(R.layout.profile_add_profile_layout, null)

        // create pop up window for add profile
        MaterialStyledDialog.Builder(this.context)
            .setIcon(R.drawable.ic_profile_blue)
            .setHeaderColor(R.color.background)
            .setTitle(getString(R.string.addANewProfileTitle))
            .setCustomView(itemView)
            .setNegativeText(getString(R.string.cancel))
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveText(getString(R.string.addANewProfileAddBtn))
            .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                val profileName = itemView.findViewById<View>(R.id.profile_name) as MaterialEditText
                val profileRelation =
                    itemView.findViewById<View>(R.id.profile_relation) as MaterialEditText

                when {
                    TextUtils.isEmpty(profileName.text.toString()) -> {
                        SnackBar.showToastBar(
                            this.context,
                            getString(R.string.profileNameEmptyError)
                        )
                        return@SingleButtonCallback
                    }
                }
                _viewModel.addProfileToDB(
                    profileName.text.toString(),
                    loggedUserObject,
                    profileRelation.text.toString()
                )
            })
            .build()
            .show()
    }

    companion object {
        fun newInstance(loggedUser: UserObject) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    loggedUserObject = loggedUser
                }
            }
    }
}