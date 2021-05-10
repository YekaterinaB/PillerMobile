package com.example.piller.utilities

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.lang.Exception

object JSONMessageExtractor {
    fun getErrorMessage(
        response: Response<ResponseBody>,
        defaultMessage: String = "An error has occurred"
    ): String {
        var errMsg = defaultMessage
        try {
            val jObjError = JSONObject(response.errorBody()!!.string())
            errMsg = jObjError["message"] as String
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return errMsg
    }
}