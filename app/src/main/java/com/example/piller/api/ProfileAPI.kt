package com.example.piller.api

import com.example.piller.models.ProfileSerializable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ProfileAPI {
    @Headers("Content-Type: application/json")

    @GET("profile/{userId}")
    fun getAllProfilesByEmail(@Path("userId") userId: String): Call<ResponseBody>

    @PUT("profile/{userId}")
    fun addProfileToUser(
        @Path("userId") userId: String,
        @Body profile: ProfileSerializable
    ): Call<ResponseBody>

    @DELETE("profile/{userId}")
    fun deleteAllProfiles(@Path("userId") userId: String): Call<ResponseBody>

    @DELETE("profile/{userId}/{profileId}")
    fun deleteProfile(
        @Path("userId") userId: String,
        @Path("profileId") profileId: String
    ): Call<ResponseBody>
}