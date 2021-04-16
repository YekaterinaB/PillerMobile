package com.example.piller.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DrugAPI {
    @Headers("Content-Type: application/json")

    @GET("drugApiCalls/drugByName/{drugName}")
    fun findDrugByName(@Path("drugName") drugName: String): Call<ResponseBody>

    @GET("drugApiCalls/findInteractions/{userId}/{profileId}/{newRxcui}")
    fun findInteractionList(
        @Path("userId") userId: String,
        @Path("profileId") profileId: String,
        @Path("newRxcui") drugName: String
    ): Call<ResponseBody>

    @GET("drugApiCalls/getDrugImage")
    fun getDrugImage(@Query("rxcui") rxcui: String): Call<ResponseBody>

    @Multipart
    @POST("drugApiCalls/findDrugByImage")
    fun findDrugByImage(@Part file: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("drugApiCalls/findDrugByBoxImage")
    fun findDrugByBox(@Part file: MultipartBody.Part): Call<ResponseBody>
}