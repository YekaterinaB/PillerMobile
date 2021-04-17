package com.example.piller.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface SupervisorsAPI {
    @Headers("Content-Type: application/json")

    @GET("supervisors/{userId}")
    fun getSupervisors(@Path("userId") userId: String): Call<ResponseBody>

    @POST("supervisors/{userId}/{supervisorName}/{supervisorEmail}")
    fun addSupervisor(
        @Path("userId") userId: String,
        @Path("supervisorName") supervisorName: String,
        @Path("supervisorEmail") supervisorEmail: String
    ): Call<ResponseBody>

    @DELETE("supervisors/{userId}/{supervisorEmail}")
    fun deleteSupervisor(
        @Path("userId") userId: String,
        @Path("supervisorEmail") supervisorEmail: String
    ): Call<ResponseBody>

    @GET("supervisors/threshold/{userId}")
    fun getThreshold(@Path("userId") userId: String): Call<ResponseBody>

    @PUT("supervisors/threshold/{userId}/{threshold}")
    fun updateThreshold(
        @Path("userId") userId: String,
        @Path("threshold") threshold: Int
    ): Call<ResponseBody>


    @DELETE("supervisors/{userId}")
    fun deleteSupervisorList(@Path("userId") userId: String): Call<ResponseBody>
}