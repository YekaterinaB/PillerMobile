package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.ProfileAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.*
import com.example.piller.utilities.notifyObserver
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    val mutableListOfProfiles: MutableLiveData<MutableList<CalendarProfile>> by lazy {
        MutableLiveData<MutableList<CalendarProfile>>(mutableListOf<CalendarProfile>())
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


    fun getListOfProfiles(): MutableList<CalendarProfile> {
        return mutableListOfProfiles.value!!
    }

    fun getListOfSecondaryProfiles(): MutableList<CalendarProfile> {
        val size=mutableListOfProfiles.value!!.size
        val  slice:MutableList<CalendarProfile>
        if(size == 0){
            //no profiles
            slice = mutableListOf<CalendarProfile>()
        }else{
            slice = mutableListOfProfiles.value!!.subList(1,size)
        }
        return slice
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
            Profile(profileObjectData.getString("id"), profileObjectData.getString("name"),
                profileObjectData.getString("relation"))
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

    fun deleteOneProfile(userId: String, profile: Profile) {
        if (getCurrentProfileName() == profile.name) {
            mutableCurrentProfile.value = mutableListOfProfiles.value!![0].getProfileObject()
        }
        deleteProfileFromProfileList(profile.profileId)
        deleteProfileFromDB(userId,profile.profileId)

    }

    private fun deleteProfileFromDB(userId:String, profileId: String) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        retrofit.deleteProfile(userId, profileId).enqueue(
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

    fun addProfileToDB(profileName: String, loggedUserObject: UserObject,profileRelation:String) {
        val retrofit = ServiceBuilder.buildService(ProfileAPI::class.java)
        val profileSerializable=ProfileSerializable(profileName,profileRelation)
        retrofit.addProfileToUser(loggedUserObject.userId, profileSerializable).enqueue(
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
