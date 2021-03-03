package com.example.piller.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DrugIntakeAPI {
    @Headers("Content-Type: application/json")

    @POST("drugIntakes/addIntake/{taken_id}/{date}")
    fun addDrugIntake(
        @Path("taken_id") taken_id: String,
        @Path("date") date: Long
    ): Call<ResponseBody>

    @DELETE("drugIntakes/removeIntake/{taken_id}/{date}")
    fun removeDrugIntake(
        @Path("taken_id") taken_id: String,
        @Path("date") date: Long
    ): Call<ResponseBody>

    @GET("drugIntakes/getAllIntakes/{taken_id}")
    fun getAllDrugIntakes(@Path("taken_id") taken_id: String): Call<ResponseBody>
}