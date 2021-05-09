package com.example.piller.api

import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugObject
import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CalendarAPI {
    @Headers(DbConstants.contentHeaders)

    @GET(DbConstants.calendarURL + "{${DbConstants.userId}}/{${DbConstants.profileIdStr}}")
    fun getCalendarByUser(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.profileIdStr) profileId: String
    ): Call<ResponseBody>

//    @PUT("calendar/{email}/{name}")
//    fun updateCalendarByUser(
//        @Path("email") email: String,
//        @Path("name") name: String,
//        @Body calendarEvent: CalendarEvent
//    ): Call<ResponseBody>

    @DELETE(DbConstants.calendarURL + "{${DbConstants.userId}}")
    fun deleteCalendarByUser(
        @Path(DbConstants.userId) userId: String,
        @Body calendarEvent: CalendarEvent
    ): Call<ResponseBody>

    @POST(DbConstants.calendarURL + "${DbConstants.addDrug}/{${DbConstants.userId}}/{${DbConstants.profileIdStr}}")
    fun addDrugCalendarByUser(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.profileIdStr) profileId: String,
        @Body drug_info: DrugObject
    ): Call<ResponseBody>

    @POST(DbConstants.calendarURL + "${DbConstants.updateDrug}/{${DbConstants.userId}}/{${DbConstants.profileIdStr}}/{${DbConstants.drugId}}")
    fun updateDrugOccurrence(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.profileIdStr) profileId: String,
        @Path(DbConstants.drugId) drug_id: String,
        @Body drug_info: DrugObject
    ): Call<ResponseBody>

    //  @Query means that it'll be in the end of the url with ?rxcui=12345
    @HTTP(
        method = DbConstants.DELETEMethod,
        path = "/" + DbConstants.calendarURL + "/${DbConstants.deleteDrug}/{${DbConstants.userId}}/{${DbConstants.profileIdStr}}",
        hasBody = true
    )
    fun deleteDrugByUser(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.profileIdStr) profileId: String,
        @Query(DbConstants.drugId) drug_id: String
    ): Call<ResponseBody>

    @PUT(DbConstants.calendarURL + "${DbConstants.deleteFutureOccurrencesOfDrugByUser}/{${DbConstants.userId}}/{${DbConstants.profileIdStr}}")
    fun deleteFutureOccurrencesOfDrugByUser(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.profileIdStr) profileId: String,
        @Query(DbConstants.drugId) drug_id: String,
        @Query(DbConstants.repeatEnd) repeatEnd: String
    ): Call<ResponseBody>
}