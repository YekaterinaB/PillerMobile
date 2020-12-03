package com.example.piller.viewModels

import androidx.lifecycle.ViewModel

class ManageSupervisorsViewModel : ViewModel() {
    private lateinit var loggedUserName :String
    private lateinit var loggedUserEmail :String

    fun setEmailAndName(email:String,name:String){
        loggedUserName=email
        loggedUserName=name
    }

}
