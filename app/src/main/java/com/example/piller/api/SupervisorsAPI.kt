package com.example.piller.api

import com.example.piller.models.CalendarEvent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface SupervisorsAPI {
    @Headers("Content-Type: application/json")

    @GET("supervisors/{email}")
    fun getSupervisors(
        @Path("email") email: String
    ): Call<ResponseBody>

    @POST("supervisors/{email}/{supervisorName}/{supervisorEmail}")
    fun addSupervisor(
        @Path("email") email: String,
        @Path("supervisorName") supervisorName: String,
        @Path("supervisorEmail") supervisorEmail: String
    ): Call<ResponseBody>

    @DELETE("supervisors/{email}/{supervisorEmail}")
    fun deleteSupervisor(
        @Path("email") email: String,
        @Path("supervisorEmail") supervisorEmail: String
    ): Call<ResponseBody>

    @GET("supervisors/threshold/{email}")
    fun getThreshold(
        @Path("email") email: String
    ): Call<ResponseBody>

    @PUT("supervisors/threshold/{email}/{threshold}")
    fun updateThreshold(
        @Path("email") email: String,
        @Path("threshold") threshold: Int
    ): Call<ResponseBody>

    @PUT("supervisors/counter/{email}/{drugName}")
    fun addMissedToCounterDrug(
        @Path("email") email: String,
        @Path("drugName") drugName: String
    ): Call<ResponseBody>

    @DELETE("supervisors/counter/{email}/{drugName}")
    fun deleteDrugCounter(
        @Path("email") email: String,
        @Path("drugName") drugName: String
    ): Call<ResponseBody>

    @DELETE("supervisors/{email}")
    fun deleteSupervisorList(
        @Path("email") email: String
    ): Call<ResponseBody>

}