package com.example.piller.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ProfileAPI {
    @Headers("Content-Type: application/json")

    @GET("profile/{email}")
    fun getAllProfilesByEmail(@Path("email") email: String): Call<ResponseBody>

    @POST("profile/{email}/{mainProfile}")
    fun initProfileList(
        @Path("email") email: String,
        @Path("mainProfile") mainProfile: String
    ): Call<ResponseBody>

    @PUT("profile/{email}/{name}")
    fun addProfileToUser(
        @Path("email") email: String,
        @Path("name") name: String
    ): Call<ResponseBody>

    @DELETE("profile/{email}")
    fun deleteAllProfiles(@Path("email") email: String): Call<ResponseBody>

    @DELETE("profile/{email}/{name}")
    fun deleteProfile(@Path("email") email: String, @Path("name") name: String): Call<ResponseBody>


}