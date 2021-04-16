package com.example.piller.api

import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CalendarAPI {
    @Headers("Content-Type: application/json")

    @GET("calendar/{userId}/{profileId}")
    fun getCalendarByUser(
        @Path("userId") userId: String,
        @Path("profileId") profileId: String
    ): Call<ResponseBody>

//    @PUT("calendar/{email}/{name}")
//    fun updateCalendarByUser(
//        @Path("email") email: String,
//        @Path("name") name: String,
//        @Body calendarEvent: CalendarEvent
//    ): Call<ResponseBody>

    @DELETE("calendar/{userId}")
    fun deleteCalendarByUser(
        @Path("userId") userId: String,
        @Body calendarEvent: CalendarEvent
    ): Call<ResponseBody>

    @POST("calendar/addDrug/{userId}/{profileId}")
    fun addDrugCalendarByUser(
        @Path("userId") userId: String,
        @Path("profileId") profileId: String,
        @Body drug_info: DrugObject
    ): Call<ResponseBody>

    @POST("calendar/updateDrug/{userId}/{profileId}/{drug_id}")
    fun updateDrugOccurrence(
        @Path("userId") userId: String,
        @Path("profileId") profileId: String,
        @Path("drug_id") drug_id: String,
        @Body drug_info: DrugObject
    ): Call<ResponseBody>

    //  @Query means that it'll be in the end of the url with ?rxcui=12345
    @HTTP(method = "DELETE", path = "/calendar/deleteDrug/{userId}/{profileId}", hasBody = true)
    fun deleteDrugByUser(
        @Path("userId") userId: String,
        @Path("profileId") profileId: String,
        @Query("drug_id") drug_id: String
    ): Call<ResponseBody>

    @PUT("calendar/deleteFutureOccurrencesOfDrugByUser/{userId}/{profileId}")
    fun deleteFutureOccurrencesOfDrugByUser(
        @Path("userId") userId: String,
        @Path("profileId") profileId: String,
        @Query("drug_id") drug_id: String,
        @Query("repeat_end") repeatEnd: String
    ): Call<ResponseBody>
}