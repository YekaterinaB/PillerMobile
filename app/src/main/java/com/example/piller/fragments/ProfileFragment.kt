package com.example.piller.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class ProfileFragment : FragmentWithUserObject() {
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var profileRecycle: RecyclerView
    private lateinit var fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false)
        setOnClickListeners()
        initRecyclersAndAdapters()
        initObservers()
        return fragmentView
    }

    private fun initObservers() {
        viewModel.mutableToastError.observe(
            viewLifecycleOwner,
            Observer { toastMessage ->
                toastMessage?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        SnackBar.showToastBar(this.requireContext(), toastMessage)
                    }
                }
            })

        viewModel.mutableListOfProfiles.observe(
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
        profileAdapter.setData(viewModel.getListOfProfiles())
        profileAdapter.notifyDataSetChanged()
    }

    private fun initRecyclersAndAdapters() {
        profileRecycle = fragmentView.findViewById(R.id.profile_list)
        // initiate list of profiles with recyclers and adapters
        val profileList = viewModel.getListOfProfiles()
        profileRecycle.layoutManager = LinearLayoutManager(fragmentView.context)
        profileAdapter = ProfileAdapter(
            profileList,
            clickOnItemListener = { changeProfile(it) },
            clickOnButtonListener = { clickOnDeleteProfile(it) })
        profileRecycle.adapter = profileAdapter
    }

    private fun setOnClickListeners() {
        fragmentView.add_profile_button.setOnClickListener {
            showAddProfileToUserWindow()
        }
    }

    private fun changeProfile(profile: Profile) {
        viewModel.setCurrentProfile(profile)
    }

    private fun clickOnDeleteProfile(profile: Profile) {
        //  todo add confirmation popup
        viewModel.deleteOneProfile(profile)
    }

    private fun showAddProfileToUserWindow() {
        val itemView = LayoutInflater.from(this.context)
            .inflate(R.layout.add_profile_layout, null)

        // create pop up window for add profile
        MaterialStyledDialog.Builder(this.context)
            .setIcon(R.drawable.ic_profile)
            .setTitle("ADD A NEW PROFILE")
            .setCustomView(itemView)
            .setNegativeText("CANCEL")
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveText("ADD PROFILE")
            .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                val profileName = itemView.findViewById<View>(R.id.profile_name) as MaterialEditText

                when {
                    TextUtils.isEmpty(profileName.text.toString()) -> {
                        SnackBar.showToastBar(
                            this.context,
                            "Profile name cannot be empty"
                        )
                        return@SingleButtonCallback
                    }
                }
                viewModel.addProfileToDB(profileName.text.toString(), loggedUserObject)
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