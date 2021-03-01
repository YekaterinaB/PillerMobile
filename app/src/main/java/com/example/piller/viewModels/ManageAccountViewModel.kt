package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.User
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ManageAccountViewModel : ViewModel() {
    private val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
    var loggedUserName = ""
    val loggedUserEmail: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val snackBarMessage: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val goToMainActivity: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    init {
        goToMainActivity.value = false
    }

    fun updateUserEmail(newEmail: String, password: String) {
        val updatedUser = User(newEmail, loggedUserName, password)
        sendRetrofitUpdateEmail(updatedUser, newEmail)
    }

    private fun sendRetrofitUpdateEmail(updatedUser: User, newEmail: String) {
        loggedUserEmail.value?.let {
            retrofit.updateUser(it, updatedUser).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        snackBarMessage.value = "Could not update user."
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() != 200) {
                            snackBarMessage.value =
                                "Error updating account. Please try again later."
                        } else {
                            loggedUserEmail.value = newEmail
                            snackBarMessage.value = "User email updated."
                        }
                    }
                }
            )
        }
    }

    fun deleteUser() {
        loggedUserEmail.value?.let {
            retrofit.deleteUser(it).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        snackBarMessage.value = "Could not delete user."
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() == 200) {
                            goToMainActivity.value = true
                        } else {
                            snackBarMessage.value =
                                "Error deleting account. Please try again later."
                        }
                    }
                }
            )
        }
    }

    fun updatePassword(updatedUser: JSONObject) {
        loggedUserEmail.value?.let {
            retrofit.updatePassword(it, updatedUser).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        snackBarMessage.value = "Could not update password."
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() != 200) {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            snackBarMessage.value = jObjError["message"] as String
                        } else {
                            snackBarMessage.value = "User password updated."
                        }
                    }
                }
            )
        }
    }

}