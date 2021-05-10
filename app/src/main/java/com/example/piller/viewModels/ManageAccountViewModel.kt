package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.UserSerializable
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.JSONMessageExtractor
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ManageAccountViewModel : ViewModel() {
    private val _retrofit = ServiceBuilder.buildService(UserAPI::class.java)

    val _snackBarMessage: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val _mutableEmail: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val _mutableUsername: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val _isDeleteSucceeded: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val _mutableIsValidInfo: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun verifyPassword(
        loggedUserObject: UserObject, oldPassword: String,
        newEmail: String, newPassword: String, newName: String
    ) {
        val updatedUser = UserSerializable(newEmail, newName, newPassword, oldPassword)
        sendRetrofitUpdateEmailUsernamePassword(loggedUserObject, updatedUser)
    }


    private fun sendRetrofitUpdateEmailUsernamePassword(
        loggedUserObject: UserObject,
        updatedUser: UserSerializable
    ) {
        _retrofit.updateEmailUsernamePassword(loggedUserObject.userId, updatedUser).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _snackBarMessage.value = DbConstants.couldNotUpdateUserError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != DbConstants.OKCode) {
                        _snackBarMessage.value = JSONMessageExtractor.getErrorMessage(response)
                    } else {
                        _snackBarMessage.value = DbConstants.updateUserSuccessfulMessage
                        _mutableUsername.value = updatedUser.mainProfileName
                        _mutableEmail.value = updatedUser.email
                        updateAppReferences(updatedUser.email, updatedUser.password)
                        _mutableIsValidInfo.value = false
                    }
                }
            }
        )
    }

    private fun updateAppReferences(email: String, password: String) {
        AppPreferences.email = email
        AppPreferences.password = password
    }

    fun deleteUser(loggedUserObject: UserObject, password: HashMap<String, String>) {
        _retrofit.deleteUser(loggedUserObject.userId, password).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _snackBarMessage.value = DbConstants.couldNotDeleteUserError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == DbConstants.OKCode) {
                        _isDeleteSucceeded.value = true
                    } else {
                        _snackBarMessage.value = JSONMessageExtractor.getErrorMessage(response)
                    }
                }
            }
        )
    }


//    fun sendRetrofitUpdatePassword(loggedUserObject: UserObject, updatedUser: User) {
//        _retrofit.updatePassword(loggedUserObject.userId, updatedUser).enqueue(
//            object : retrofit2.Callback<ResponseBody> {
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    _snackBarMessage.value = "Could not update password."
//                }
//
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//                    if (response.raw().code() != 200) {
//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        _snackBarMessage.value = jObjError["message"] as String
//                    } else {
//                        _snackBarMessage.value = "User password updated."
//                    }
//                }
//            }
//        )
//    }
//}

//private fun sendRetrofitUpdateEmail(
//        loggedUserObject: UserObject,
//        updatedUser: User
//    ) {
//        _retrofit.updateUserEmail(loggedUserObject.userId, updatedUser).enqueue(
//            object : retrofit2.Callback<ResponseBody> {
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    _snackBarMessage.value = "Could not update user."
//                }
//
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//                    if (response.raw().code() != 200) {
//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        _snackBarMessage.value = jObjError["message"] as String
//                    } else {
//                        _snackBarMessage.value = "User email updated."
//                        _mutableEmail.value = updatedUser.email
//                    }
//                }
//            }
//        )
}