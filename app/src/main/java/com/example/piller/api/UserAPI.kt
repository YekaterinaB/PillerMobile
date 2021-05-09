package com.example.piller.api

import com.example.piller.models.UserSerializable
import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface UserAPI {
    @Headers(DbConstants.contentHeaders)

    @POST(DbConstants.userURL + DbConstants.register)
    fun registerUser(@Body user: UserSerializable): Call<ResponseBody>

    @POST(DbConstants.userURL + DbConstants.authenticate)
    fun loginUser(@Body user: UserSerializable): Call<ResponseBody>

    @HTTP(
        method = DbConstants.DELETEMethod,
        path = DbConstants.userURL + "{${DbConstants.userId}}",
        hasBody = true
    )
    fun deleteUser(
        @Path(DbConstants.userId) userId: String,
        @Body password: HashMap<String, String>
    ): Call<ResponseBody>

    @POST(DbConstants.userURL + "{${DbConstants.userId}}")
    fun updateEmailUsernamePassword(
        @Path(DbConstants.userId) userId: String,
        @Body user: UserSerializable
    ): Call<ResponseBody>

    @GET(DbConstants.userURL + "${DbConstants.resetPassword}/{${DbConstants.email}}")
    fun resetPassword(@Path(DbConstants.email) email: String): Call<ResponseBody>

//    @PUT("user/{userId}")
//    fun updateUserEmail(@Path("userId") userId: String, @Body user: User): Call<ResponseBody>
//
//    @PUT("user/updatePassword/{userId}")
//    fun updatePassword(@Path("userId") userId: String, @Body user: User): Call<ResponseBody>

    //google login
    @POST("user/googleUser/getGoogleAccount")
    fun getGoogleUser(@Body user: UserSerializable): Call<ResponseBody>

    @HTTP(method = DbConstants.DELETEMethod, path = "user/googleUser/{userId}", hasBody = false)
    fun deleteGoogleUser(@Path("userId") userId: String): Call<ResponseBody>

}