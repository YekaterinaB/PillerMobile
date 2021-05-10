package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.UserSerializable
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.JSONMessageExtractor
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
            email = email, mainProfileName = name, password = password, oldPassword = password
        )
        retrofit.registerUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        mutableToastError.value = DbConstants.existingUserEmailError
                        mutableActivitySignUpChangeResponse.value = false
                    } else {
                        mutableToastError.value = DbConstants.successfulRegistrationMessage
                        mutableActivitySignUpChangeResponse.value = true
                    }
                }
            }
        )
    }


    fun loginUser(email: String, password: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        val user = UserSerializable(
            email = email, mainProfileName = DbConstants.defaultStringValue, password = password,
            oldPassword = password
        )
        retrofit.loginUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        mutableToastError.value = DbConstants.userDoesNotExistError
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
            email = email,
            mainProfileName = mainProfileName,
            password = DbConstants.defaultStringValue,
            oldPassword = DbConstants.defaultStringValue
        )
        retrofit.getGoogleUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mutableToastError.value = DbConstants.couldNotConnectServerError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        mutableToastError.value = DbConstants.problemWithUserError
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
                    mutableToastError.value = DbConstants.couldNotResetPasswordError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        mutableToastError.value = JSONMessageExtractor.getErrorMessage(response)
                    } else {
                        mutableToastError.value = DbConstants.resetEmailSent
                    }
                }
            }
        )
    }
}
