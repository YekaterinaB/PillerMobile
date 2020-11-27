package com.example.piller.api

import com.example.piller.models.CalendarEvent
import com.example.piller.models.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CalendarAPI {
    @Headers("Content-Type: application/json")

    @GET("calendar/{email}/{name}")
    fun getCalendarByUser(
        @Path("email") email: String,
        @Path("name") name: String
    ): Call<ResponseBody>

    @PUT("calendar/{email}/{name}")
    fun updateCalendarByUser(
        @Path("email") email: String,
        @Path("name") name: String,
        @Body calendarEvent: CalendarEvent
    ): Call<ResponseBody>

    @DELETE("calendar/{email}")
    fun deleteCalendarByUser(
        @Path("email") email: String,
        @Body calendarEvent: CalendarEvent
    ): Call<ResponseBody>
}