package com.example.piller.Retrofit

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IMyService {
    @POST ("register")
    @FormUrlEncoded
    fun registerUser(@Field("email")email:String,
                     @Field("name")name:String,
                     @Field("password")password:String)

    @POST ("login")
    @FormUrlEncoded
    fun loginUser(@Field("email")email:String,
                     @Field("password")password:String)
}