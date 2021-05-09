package com.example.piller.api

import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DrugIntakeAPI {
    @Headers(DbConstants.contentHeaders)

    @POST(DbConstants.drugIntakesURL + "${DbConstants.setIntakeTaken}/{${DbConstants.takenId}}/{${DbConstants.refillId}}/{${DbConstants.intakeDate}}")
    fun setIntakeTaken(
        @Path(DbConstants.takenId) taken_id: String,
        @Path(DbConstants.refillId) refill_id: String,
        @Path(DbConstants.intakeDate) date: Long
    ): Call<ResponseBody>

    @POST(DbConstants.drugIntakesURL + "${DbConstants.setIntakeNotTaken}/{${DbConstants.takenId}}/{${DbConstants.refillId}}/{${DbConstants.intakeDate}}")
    fun setIntakeNotTaken(
        @Path(DbConstants.takenId) taken_id: String,
        @Path(DbConstants.refillId) refill_id: String,
        @Path(DbConstants.intakeDate) date: Long
    ): Call<ResponseBody>

    @GET(DbConstants.drugIntakesURL + "${DbConstants.getAllIntakes}/{${DbConstants.takenId}}")
    fun getAllDrugIntakes(@Path(DbConstants.takenId) taken_id: String): Call<ResponseBody>
}