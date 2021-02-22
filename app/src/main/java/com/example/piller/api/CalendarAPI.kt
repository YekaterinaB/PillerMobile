package com.example.piller.api

import com.example.piller.models.CalendarEvent
import com.example.piller.models.Drug
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

    @POST("calendar/addDrug/{email}/{name}")
    fun addDrugCalendarByUser(
        @Path("email") email: String,
        @Path("name") name: String,
        @Body drug_info: Drug
    ): Call<ResponseBody>

    //  @Query means that it'll be in the end of the url with ?rxcui=12345
    @HTTP(method = "DELETE", path = "/calendar/deleteDrug/{email}/{name}", hasBody = true)
    fun deleteDrugByUser(
        @Path("email") email: String,
        @Path("name") name: String,
        @Query("rxcui") drugRxcui: String
    ): Call<ResponseBody>

    @PUT("calendar/deleteFutureOccurrencesOfDrugByUser/{email}/{name}")
    fun deleteFutureOccurrencesOfDrugByUser(
        @Path("email") email: String,
        @Path("name") name: String,
        @Query("rxcui") rxcui: String,
        @Query("repeat_end") repeatEnd: String
    ): Call<ResponseBody>
}