package com.example.piller.api

import com.example.piller.models.User
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*


interface UserAPI {
    @Headers("Content-Type: application/json")

    @POST("user/register")
    fun registerUser(@Body user: User): Call<ResponseBody>

    @POST("user/authenticate")
    fun loginUser(@Body user: User): Call<ResponseBody>

    @HTTP(method = "DELETE", path = "user/{userId}", hasBody = true)
    fun deleteUser(
        @Path("userId") userId: String,
        @Body password: HashMap<String, String>
    ): Call<ResponseBody>

    @POST("user/{userId}")
    fun updateEmailUsernamePassword(
        @Path("userId") userId: String,
        @Body user: User
    ): Call<ResponseBody>

    @GET("user/resetPassword/{email}")
    fun resetPassword(@Path("email") email: String): Call<ResponseBody>

//    @PUT("user/{userId}")
//    fun updateUserEmail(@Path("userId") userId: String, @Body user: User): Call<ResponseBody>
//
//    @PUT("user/updatePassword/{userId}")
//    fun updatePassword(@Path("userId") userId: String, @Body user: User): Call<ResponseBody>

    //google login
    @POST("user/googleUser/getGoogleAccount")
    fun getGoogleUser(@Body user: User): Call<ResponseBody>

    @HTTP(method = "DELETE", path = "user/googleUser/{userId}", hasBody = false)
    fun deleteGoogleUser(@Path("userId") userId: String): Call<ResponseBody>

}