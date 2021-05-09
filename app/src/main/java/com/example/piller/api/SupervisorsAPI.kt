package com.example.piller.api

import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface SupervisorsAPI {
    @Headers(DbConstants.contentHeaders)

    @GET(DbConstants.supervisorsURL + "{${DbConstants.userId}}")
    fun getSupervisors(@Path(DbConstants.userId) userId: String): Call<ResponseBody>

    @POST(DbConstants.supervisorsURL + "{${DbConstants.userId}}/{${DbConstants.supervisorName}}/{${DbConstants.supervisorEmail}}")
    fun addSupervisor(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.supervisorName) supervisorName: String,
        @Path(DbConstants.supervisorEmail) supervisorEmail: String
    ): Call<ResponseBody>

    @DELETE(DbConstants.supervisorsURL + "{${DbConstants.userId}}/{${DbConstants.supervisorEmail}}")
    fun deleteSupervisor(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.supervisorEmail) supervisorEmail: String
    ): Call<ResponseBody>

    @GET(DbConstants.supervisorsURL + "${DbConstants.threshold}/{${DbConstants.userId}}")
    fun getThreshold(@Path(DbConstants.userId) userId: String): Call<ResponseBody>

    @PUT(DbConstants.supervisorsURL + "${DbConstants.threshold}/{${DbConstants.userId}}/{${DbConstants.threshold}}")
    fun updateThreshold(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.threshold) threshold: Int
    ): Call<ResponseBody>


    @DELETE(DbConstants.supervisorsURL + "{${DbConstants.userId}}")
    fun deleteSupervisorList(@Path(DbConstants.userId) userId: String): Call<ResponseBody>
}