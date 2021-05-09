package com.example.piller.api

import com.example.piller.utilities.DbConstants
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DrugAPI {
    @Headers(DbConstants.contentHeaders)

    @GET(DbConstants.drugApiCallsURL + "${DbConstants.drugByName}/{${DbConstants.drugNameStr}}")
    fun findDrugByName(@Path(DbConstants.drugNameStr) drugName: String): Call<ResponseBody>

    @GET("${DbConstants.drugApiCallsURL}${DbConstants.findInteractions}/{${DbConstants.userId}}/{${DbConstants.profileIdStr}}/{${DbConstants.newRxcui}}")
    fun findInteractionList(
        @Path(DbConstants.userId) userId: String,
        @Path(DbConstants.profileIdStr) profileId: String,
        @Path(DbConstants.newRxcui) drugName: String
    ): Call<ResponseBody>

    @GET(DbConstants.drugApiCallsURL + DbConstants.getDrugImage)
    fun getDrugImage(@Query(DbConstants.rxcui) rxcui: String): Call<ResponseBody>

    @Multipart
    @POST(DbConstants.drugApiCallsURL + DbConstants.findDrugByImage)
    fun findDrugByImage(@Part file: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST(DbConstants.drugApiCallsURL + DbConstants.findDrugByBoxImage)
    fun findDrugByBox(@Part file: MultipartBody.Part): Call<ResponseBody>
}