package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.ProfileAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import com.example.piller.models.CalendarProfile
import com.example.piller.models.Profile
import com.example.piller.models.UserObject
import com.example.piller.utilities.notifyObserver
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    val mutableListOfProfiles: MutableLiveData<MutableList<CalendarProfile>> by lazy {
        MutableLiveData<MutableList<CalendarProfile>>()
    }
    val mutableCurrentProfile: MutableLiveData<Profile> by lazy {
        MutableLiveData<Profile>()
    }

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private lateinit var loggedEmail: String

    fun getCurrentProfileName(): String {
        return mutableCurrentProfile.value!!.name
    }

    fun getCurrentEmail(): String {
        return loggedEmail
    }


    fun getListOfProfiles(): MutableList<CalendarProfile> {
        return mutableListOfProfiles.value!!
    }

    fun setCurrentProfile(profile: Profile) {
        mutableCurrentProfile.value = profile
    }

    fun setCurrentProfileAndEmail(profile: Profile, email: String) {
        setCurrentProfile(profile)
        loggedEmail = email
    }

    fun currentProfileUpdated() {
        // if profile was updated, is initialize in profile will turn to false
        val list = getListOfProfiles()
        val curProfile = getCurrentProfileName()
        //find profile from list
        for (i in 0 until list.size) {
            if (list[i].getProfileName() == curProfile) {
                list[i].setIsInitialized(false)
            }
        }
    }

    fun getCurrentProfile(): CalendarProfile {
        val list = getListOfProfiles()
        val curProfile = getCurrentProfileName()
        var profile =
            CalendarProfile(
                mutableCurrentProfile.value!!,
                Array(7) { mutableListOf<CalendarEvent>() },
                emptyArray()
            )

        //find profile from list
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
                list[i].setIsInitialized()
                break
            }
        }
    }

    private fun initSecondaryProfileList(response: Response<ResponseBody>) {
        val jObject = JSONObject(response.body()!!.string())
        val profileListBody = jObject.get("profile_list") as JSONArray
        for (i in 0 until profileListBody.length()) {
            addProfileToProfileList(profileListBody[i] as JSONObject)
        }
    }

    fun addProfileToProfileList(profileObjectData: JSONObject) {
        val profile =
            Profile(profileObjectData.getString("id"), profileObjectData.getString("name"))
        mutableListOfProfiles.value!!.add(
            CalendarProfile(
                profile,
                Array(7) { mutableListOf<CalendarEvent>() },
                emptyArray()
            )
        )
        mutableListOfProfiles.notifyObserver()
    }

    private fun deleteProfileFromProfileList(profileId: String) {
        for (i in 0 until mutableListOfProfiles.value!!.size) {
            if (mutableListOfProfiles.value!![i].getProfileObject().profileId == profileId) {
                mutableListOfProfiles.value!!.removeAt(i)
                break
            }
        }
        mutableListOfProfiles.notifyObserver()
    }

    fun deleteOneProfile(profile: Profile) {
        if (getCurrentProfileName() == profile.name) {
            mutableCurrentProfile.value = mutableListOfProfiles.value!![0].getProfileObject()
        }
        deleteProfileFromProfileList(profile.profileId)
        deleteProfileFromDB(profile.profileId)
    }

    private fun deleteProfileFromDB(profileId: String) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.deleteProfile(loggedEmail, profileId).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Could not delete profile."
                    }
                }
            }
        )
    }

    fun getProfileListFromDB(loggedUserObject: UserObject) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.getAllProfilesByEmail(loggedUserObject.userId).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Could not init profile list."
                    } else {
                        initSecondaryProfileList(response)
                    }
                }
            }
        )
    }

    fun addProfileToDB(profileName: String, loggedUserObject: UserObject) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.addProfileToUser(loggedUserObject.userId, profileName).enqueue(
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
                        val profileObjectData = JSONObject(response.body()!!.string())
                        addProfileToProfileList(profileObjectData)
                    }
                }
            }
        )
    }
}
