package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.SupervisorsAPI
import com.example.piller.models.Supervisor
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.notifyObserver
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class SupervisorsViewModel : ViewModel() {
    val _mutableSupervisorList: MutableLiveData<MutableList<Supervisor>> by lazy {
        MutableLiveData<MutableList<Supervisor>>(mutableListOf())
    }

    val _mutableSupervisorThreshold: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(DbConstants.DEFAULT_SUPERVISOR_THRESHOLD)
    }

    val _mutableToastError: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    fun getSupervisorsFromDB(userId: String) {
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.getSupervisors(userId).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        _mutableToastError.value = jObjError["message"] as String
                    } else {
                        val jObject = JSONObject(response.body()!!.string())
                        val supervisors = jObject.get(DbConstants.supervisorsList) as JSONArray
                        for (i in 0 until supervisors.length()) {
                            val name = supervisors.getJSONObject(i)
                                .get(DbConstants.supervisorName) as String
                            val email =
                                supervisors.getJSONObject(i)
                                    .get(DbConstants.supervisorEmail) as String
                            val isPending =
                                supervisors.getJSONObject(i).get(DbConstants.isConfirmed) as Boolean
                            _mutableSupervisorList.value!!.add(Supervisor(name, email, isPending))
                        }
                        _mutableSupervisorList.notifyObserver()
                        getThresholdFromDB(userId)
                    }
                }
            }
        )
    }


    private fun addSupervisorsToList(supervisorName: String, supervisorEmail: String) {
        _mutableSupervisorList.value!!.add(
            Supervisor(supervisorName, supervisorEmail, false)
        )

        _mutableSupervisorList.notifyObserver()
    }

    fun addSupervisorsToDB(supervisorName: String, supervisorEmail: String, userId: String) {
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.addSupervisor(userId, supervisorName, supervisorEmail).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        _mutableToastError.value = jObjError["message"] as String
                    } else {
                        addSupervisorsToList(supervisorName, supervisorEmail)
                    }
                }
            }
        )
    }

    private fun deleteSupervisorFromList(supervisorEmail: String) {
        for (i in 0 until _mutableSupervisorList.value!!.size) {
            if (_mutableSupervisorList.value!![i].getsupervisorEmail() == supervisorEmail) {
                _mutableSupervisorList.value!!.removeAt(i)
                break
            }
        }
        _mutableSupervisorList.notifyObserver()
    }

    fun deleteSupervisorsFromDB(supervisorEmail: String, userId: String) {
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.deleteSupervisor(userId, supervisorEmail).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        _mutableToastError.value = jObjError["message"] as String
                    } else {
                        deleteSupervisorFromList(supervisorEmail)
                    }
                }
            }
        )
    }


    fun getThresholdFromDB(userId: String) {
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.getThreshold(userId).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        _mutableToastError.value = DbConstants.cantGetThresholdError
                    } else {
                        val jObject = JSONObject(response.body()!!.string())
                        val threshold = jObject.get(DbConstants.threshold) as Int
                        _mutableSupervisorThreshold.value = threshold
                    }
                }
            }
        )
    }


    fun updateThresholdInDB(stringThreshold: String, userId: String) {
        val threshold: Int
        if (stringThreshold == DbConstants.noThreshold) {
            threshold = 0
        } else {
            threshold = stringThreshold.toInt()
        }
        val retrofit = ServiceBuilder.buildService(SupervisorsAPI::class.java)
        retrofit.updateThreshold(userId, threshold).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        _mutableToastError.value = DbConstants.cantUpdateThresholdError
                    } else {
                        _mutableSupervisorThreshold.value = threshold
//                        mutableToastError.value =
//                            "Selected missed days before notification: $stringThreshold"
                    }
                }
            }
        )
    }
}
