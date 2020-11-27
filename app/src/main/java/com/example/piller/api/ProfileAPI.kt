package com.example.piller.api

import com.example.piller.models.CalendarEvent
import com.example.piller.models.Profile
import com.example.piller.models.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ProfileAPI {
    @Headers("Content-Type: application/json")

    @GET("profile/{email}")
    fun getAllProfilesByEmail(@Path("email")email:String): Call<ResponseBody>

    @POST("profile/{email}")
    fun addProfileToUser(@Body profile: Profile): Call<ResponseBody>

    @DELETE("profile/{email}")
    fun deleteAllProfiles(@Path("email")email:String): Call<ResponseBody>

    @DELETE("profile/{email}/{name}")
    fun deleteProfile(@Path("email")email:String,@Path("name")name:String): Call<ResponseBody>


}