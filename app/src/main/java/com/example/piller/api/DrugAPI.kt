package com.example.piller.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface DrugAPI {
    @Headers("Content-Type: application/json")

    @GET("drugApiCalls/drugByName/{drugName}")
    fun findDrugByName(@Path("drugName") drugName: String): Call<ResponseBody>
}