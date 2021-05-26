package com.example.piller.api

import com.example.piller.models.ProfileSerializable
import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ProfileAPI {
    @Headers(DbConstants.contentHeaders)

    @GET(DbConstants.profileURL + "{${DbConstants.userId}}")
    fun getAllProfilesByEmail(@Path(DbConstants.userId) userId: String): Call<ResponseBody>

    @PUT(DbConstants.profileURL + "{${DbConstants.userId}}")
    fun addProfileToUser(
        @Path(DbConstants.userId) userId: String,
        @Body profile: ProfileSerializable
    ): Call<ResponseBody>

    @DELETE(DbConstants.profileURL + "{${DbConstants.userId}}")
    fun deleteAllProfiles(@Path(DbConstants.userId) userId: String): Call<ResponseBody>

    @DELETE(DbConstants.profileURL + "{${DbConstants.userId}}/{${DbConstants.profileIdStr}}")
    fun deleteProfile(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.profileIdStr) profileId: String
    ): Call<ResponseBody>
}