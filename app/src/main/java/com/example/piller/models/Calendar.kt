package com.example.piller.models

import com.google.gson.annotations.SerializedName

data class Calendar(
    @SerializedName("email") var email: String,
    @SerializedName("name")var name: String
    //@SerializedName("drug_list")var drugList: List
)
