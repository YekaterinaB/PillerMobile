package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.SupervisorsAPI
import com.example.piller.models.CalendarEvent
import com.example.piller.models.Profile
import com.example.piller.models.Supervisor
import com.example.piller.utilities.notifyObserver
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ManageSupervisorsViewModel : ViewModel() {
    private lateinit var loggedUserName: String
    private lateinit var loggedUserEmail: String

    val mutableSupervisorList: MutableLiveData<MutableList<Supervisor>> by lazy {
        MutableLiveData<MutableList<Supervisor>>()
    }

    val mutableSupervisorThreshold: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setEmailAndName(email: String, name: String) {
        loggedUserEmail = email
        loggedUserName = name
    }

    fun getSupervisorsFromDB(){
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.getSupervisors(loggedUserEmail).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Could not get supervisors."
                    }else{
                        val jObject = JSONObject(response.body()!!.string())
                        val supervisors=jObject.get("supervisorsList") as JSONArray
                        for (i in 0 until supervisors.length()) {
                            val name= supervisors.getJSONObject(i).get("supervisorName") as String
                            val email=supervisors.getJSONObject(i).get("supervisorEmail") as String
                            mutableSupervisorList.value!!.add(Supervisor(name,email))
                        }
                        mutableSupervisorList.notifyObserver()

                    }
                }
            }
        )
    }

    fun addSupervisorsToDB(supervisorName:String,supervisorEmail:String){
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.addSupervisor(loggedUserEmail,supervisorName,supervisorEmail).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Could not add supervisor."
                    }
                }
            }
        )
    }

    fun deleteSupervisorsToDB(supervisorEmail:String){
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.deleteSupervisor(loggedUserEmail,supervisorEmail).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Could not delete supervisor."
                    }
                }
            }
        )
    }



    fun getThresholdFromDB(){
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.getThreshold(loggedUserEmail).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Could not get threshold for supervisors."
                    } else {
                        val jObject = JSONObject(response.body()!!.string())
                        val threshold = jObject.get("threshold") as Int
                        mutableSupervisorThreshold.value = threshold
                    }
                }
            }
        )
    }


    fun updateThresholdInDB(
        stringThreshold: String
    ) {
        val threshold:Int
        if (stringThreshold == "None") {
            threshold = -1
        } else {
            threshold = stringThreshold.toInt()
        }
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.updateThreshold(loggedUserEmail, threshold).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "Could not update threshold for supervisors."
                    } else {
                        mutableSupervisorThreshold.value = threshold
                        mutableToastError.value =
                            "Selected missed days before notification: $stringThreshold"
                    }
                }
            }
        )
    }

}
