package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.ProfileAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import com.example.piller.models.Profile
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    private val profileList = mutableListOf<Profile>()
    val mutableCurrentProfile : MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }
    lateinit var currentProfile:String
    private lateinit var loggedEmail: String


    fun getCurrentProfile():Profile{
        val list = getProfileList()
        val curProfile=getCurrentProfileName()
        var profile=Profile(curProfile, emptyArray())
        for (i in 0 until list.size) {
            if (list[i].getProfileName() == curProfile) {
                profile = list[i]
            }
        }
        return profile
    }

    fun changeProfileCalendar(weeklyCalendar:Array<MutableList<CalendarEvent>>){
        val list = getProfileList()
        val curProfile=getCurrentProfileName()
        for (i in 0 until list.size) {
            if (list[i].getProfileName() == curProfile) {
                list[i].setWeeklyCalendar(weeklyCalendar)
                break
            }
        }
    }

    fun getCurrentProfileName(): String {
      return currentProfile
    }

    fun getCurrentEmail(): String {
        return loggedEmail
    }
    

    fun getProfileList(): MutableList<Profile> {
        return profileList
    }

    fun changeCurrentProfileLiveAndRegular(profile: String) {
        mutableCurrentProfile.value = profile
        currentProfile=profile
    }

    fun setProfileAndEmail(profile: String, email: String) {
        changeCurrentProfileLiveAndRegular(profile)
        loggedEmail = email
    }


    fun getProfileListByUser(mainProfile: String) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.getAllProfilesByEmail(loggedEmail).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //todo
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        initProfileList(response, mainProfile)
                    }
                }
            }
        )
    }

    private fun initProfileList(
        response: Response<ResponseBody>, mainProfile: String
    ) {
        profileList.add(
            Profile(
                mainProfile,
                emptyArray()
            )
        )
        val jObject = JSONObject(response.body()!!.string())
        val profileListBody = jObject.get("profile_list") as JSONArray
        for (i in 0 until profileListBody.length()) {
            profileList.add(
                Profile(
                    profileListBody[i].toString(),
                    emptyArray()
                )
            )
        }

    }

}