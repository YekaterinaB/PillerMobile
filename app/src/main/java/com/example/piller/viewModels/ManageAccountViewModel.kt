package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.User
import com.example.piller.models.UserObject
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ManageAccountViewModel : ViewModel() {
    private val retrofit = ServiceBuilder.buildService(UserAPI::class.java)

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

    fun updateUserEmail(loggedUserObject: UserObject, newEmail: String, password: String) {
        val updatedUser = User(newEmail, loggedUserObject.currentProfile!!.name, password)
        sendRetrofitUpdateEmail(loggedUserObject, updatedUser, newEmail)
    }

    private fun sendRetrofitUpdateEmail(
        loggedUserObject: UserObject,
        updatedUser: User,
        newEmail: String
    ) {
        retrofit.updateUser(loggedUserObject.email, updatedUser).enqueue(
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
                        loggedUserObject.email = newEmail
                        loggedUserEmail.value = newEmail
                        snackBarMessage.value = "User email updated."
                    }
                }
            }
        )
    }

    fun deleteUser(loggedUserObject: UserObject) {
        retrofit.deleteUser(loggedUserObject.email).enqueue(
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
                        snackBarMessage.value = "Error deleting account. Please try again later."
                    }
                }
            }
        )
    }


    fun updatePassword(loggedUserObject: UserObject, updatedUser: JSONObject) {
        retrofit.updatePassword(loggedUserObject.email, updatedUser).enqueue(
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