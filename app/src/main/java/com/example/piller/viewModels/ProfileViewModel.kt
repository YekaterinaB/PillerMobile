package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.ProfileAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import com.example.piller.models.Profile
import com.example.piller.utilities.notifyObserver
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    val mutableListOfProfiles: MutableLiveData<MutableList<Profile>> by lazy {
        MutableLiveData<MutableList<Profile>>()
    }
    val mutableCurrentProfileName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private lateinit var loggedEmail: String

    fun getCurrentProfileName(): String {
        return mutableCurrentProfileName.value!!
    }

    fun getCurrentEmail(): String {
        return loggedEmail
    }


    fun getListOfProfiles(): MutableList<Profile> {
        return mutableListOfProfiles.value!!
    }

    fun changeCurrentProfileName(profile: String) {
        mutableCurrentProfileName.value = profile
    }

    fun setMainProfileAndEmail(profile: String, email: String) {
        changeCurrentProfileName(profile)
        loggedEmail = email
    }


    fun getCurrentProfile(): Profile {
        val list = getListOfProfiles()
        val curProfile = getCurrentProfileName()
        var profile = Profile(curProfile, Array(7) { mutableListOf<CalendarEvent>() })
        for (i in 0 until list.size) {
            if (list[i].getProfileName() == curProfile) {
                profile = list[i]
            }
        }
        return profile
    }

    fun changeCalendarForCurrentProfile(weeklyCalendar: Array<MutableList<CalendarEvent>>) {
        val list = getListOfProfiles()
        val curProfile = getCurrentProfileName()
        for (i in 0 until list.size) {
            if (list[i].getProfileName() == curProfile) {
                list[i].setWeeklyCalendar(weeklyCalendar)
                // profile has been initialize flag
                list[i].profileInitialized()
                break
            }
        }
    }



    fun addProfileToProfileList(profieName: String) {
        mutableListOfProfiles.value!!.add(
            Profile(
                profieName,
                Array(7) { mutableListOf<CalendarEvent>() }
            )
        )
        mutableListOfProfiles.notifyObserver()
    }

    private fun deleteProfileFromProfileList(profieName: String) {
        for (i in 0 until mutableListOfProfiles.value!!.size) {
            if (mutableListOfProfiles.value!![i].getProfileName() == profieName) {
                mutableListOfProfiles.value!!.removeAt(i)
            }
        }
        mutableListOfProfiles.notifyObserver()
    }


    private fun initProfileList(
        response: Response<ResponseBody>, mainProfile: String
    ) {
        addProfileToProfileList(mainProfile)
        val jObject = JSONObject(response.body()!!.string())
        val profileListBody = jObject.get("profile_list") as JSONArray
        for (i in 0 until profileListBody.length()) {
            addProfileToProfileList(
                profileListBody[i].toString()
            )

        }

    }

    fun getProfileListFromDB(mainProfile: String) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.getAllProfilesByEmail(loggedEmail).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Could not get profile list."
                    } else {
                        initProfileList(response, mainProfile)
                    }
                }
            }
        )
    }

    fun addProfileToDB(profileName: String) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.addProfileToUser(loggedEmail, profileName).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Profile name already exists"
                    } else {
                        addProfileToProfileList(profileName)
                    }
                }
            }
        )
    }


}