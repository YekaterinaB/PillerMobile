package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.UserSerializable
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class LoginActivityViewModel : ViewModel() {
    val mutableToastError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val mutableActivityLoginChangeResponse: MutableLiveData<Response<ResponseBody>> by lazy {
        MutableLiveData<Response<ResponseBody>>()
    }

    val mutableActivitySignUpChangeResponse: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    fun registerUser(email: String, name: String, password: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        val user = UserSerializable(
            email = email, mainProfileName = name, password = password,
            oldPassword = password
        )
        retrofit.registerUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "A user with this email already exists."
                        mutableActivitySignUpChangeResponse.value = false
                    } else {
                        mutableToastError.value = "Your account has been successfully created."
                        mutableActivitySignUpChangeResponse.value = true
                    }
                }
            }
        )
    }


    fun loginUser(email: String, password: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        val user = UserSerializable(
            email = email, mainProfileName = "", password = password,
            oldPassword = password
        )
        retrofit.loginUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value =
                            "User does not exist, check your login information."
                    } else {
                        mutableActivityLoginChangeResponse.value = response
                        //  remember email and password if the user wants to
                        updateAppPreferences(true, email, password)
                    }
                }
            }
        )
    }

    fun getGoogleUser(email: String, mainProfileName: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        val user = UserSerializable(
            email = email, mainProfileName = mainProfileName, password = "",
            oldPassword = ""
        )
        retrofit.getGoogleUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not connect to server."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        mutableToastError.value = "There is a problem with user."
                    } else {
                        mutableActivityLoginChangeResponse.value = response
                        //  remember email and password if the user wants to
                        //updateAppPreferences(true, email, password)
                    }
                }
            }
        )
    }

    private fun updateAppPreferences(stayLogged: Boolean, email: String, password: String) {
        AppPreferences.stayLoggedIn = stayLogged
        AppPreferences.email = email
        AppPreferences.password = password
    }

    fun sendEmailToResetPassword(email: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        retrofit.resetPassword(email).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = "Could not reset password."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        mutableToastError.value = jObjError["message"] as String
                    } else {
                        mutableToastError.value = "Reset email sent!"
                    }
                }
            }
        )
    }
}
