package com.example.piller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.listAdapters.ProfileAdapter
import com.example.piller.viewModels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment: Fragment() {
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var profileAdapter : ProfileAdapter
    private lateinit var profileRecycle : RecyclerView
    private lateinit var fragmentView:View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView= inflater.inflate(R.layout.fragment_profile, container, false)
        //setOnClickListeners()
        initRecyclersAndAdapters()
        return fragmentView
    }
    fun initRecyclersAndAdapters() {
        profileRecycle=fragmentView.findViewById(R.id.profile_list)

        val profileList=viewModel.getProfileList()
        profileRecycle.layoutManager=LinearLayoutManager(fragmentView.context)
        profileAdapter= ProfileAdapter(profileList,clickListener={clickOnProfile(it)})
        profileRecycle.adapter =profileAdapter
    }

    private fun setOnClickListeners() {
        add_profile_button.setOnClickListener {
            //viewModel.addProfileToUser()
        }

    }

    fun clickOnProfile(profileName:String){
        viewModel.changeCurrentProfileLiveAndRegular(profileName)
    }

    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }
}