package com.example.piller.viewModels

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.activities.LoginActivity
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.User
import com.example.piller.models.UserObject
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

    fun verifyPassword(
        loggedUserObject: UserObject, oldPassword: String,
        newEmail: String, newPassword: String, newName: String
    ) {
        val updatedUser = User(newEmail, newName, newPassword, oldPassword)
        sendRetrofitUpdateEmailUsernamePassword(loggedUserObject, updatedUser)
    }


    private fun sendRetrofitUpdateEmailUsernamePassword(
        loggedUserObject: UserObject,
        updatedUser: User
    ) {
        _retrofit.updateEmailUsernamePassword(loggedUserObject.userId, updatedUser).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _snackBarMessage.value = "Could not update user."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        _snackBarMessage.value = jObjError["message"] as String
                    } else {
                        _snackBarMessage.value = "User was updated successfully."
                        _mutableUsername.value = updatedUser.mainProfileName
                        _mutableEmail.value = updatedUser.email
                        updateAppRefrences(updatedUser.email,updatedUser.password)
                    }
                }
            }
        )
    }
    private fun updateAppRefrences(email:String,password:String){
        AppPreferences.email= email
        AppPreferences.password= password

    }



    fun deleteUser(loggedUserObject: UserObject,password: HashMap<String,String>) {
        _retrofit.deleteUser(loggedUserObject.userId, password).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _snackBarMessage.value = "Could not delete user."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        _isDeleteSucceeded.value=true;

                    } else {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        _snackBarMessage.value = jObjError["message"] as String
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