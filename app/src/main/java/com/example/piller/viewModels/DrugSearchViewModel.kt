package com.example.piller.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.DrugAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.DrugOccurrence
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.File

class DrugSearchViewModel : ViewModel() {
    private val drugAPIRetrofit = ServiceBuilder.buildService(DrugAPI::class.java)

    val drugsInteractionResult: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    //  this will hold the name that the user searched but no drug was found
    val drugSearchNoResult: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val drugsSearchResult: MutableLiveData<MutableList<DrugOccurrence>> by lazy {
        MutableLiveData<MutableList<DrugOccurrence>>()
    }

    val snackBarMessage: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val newDrug: MutableLiveData<DrugOccurrence> by lazy {
        MutableLiveData<DrugOccurrence>()
    }

    val addedDrugSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }


    fun getDrugByRxcui(rxcui: Int): DrugOccurrence? {
        val filteredArray = drugsSearchResult.value?.filter { drug -> drug.rxcui == rxcui }
        if (filteredArray != null && filteredArray.isNotEmpty()) {
            return filteredArray[0]
        }
        return null
    }

    private fun getFileAsMultiPart(imageFile: File): MultipartBody.Part {
        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse("multipart/form-data"), imageFile
        )

        return MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
    }

    fun searchDrugByImage(imageFilePath: String) {
        val imageFile = File(imageFilePath)

        if (imageFile.exists() && imageFile.isFile) {
            val multipartBody = getFileAsMultiPart(imageFile)
            drugsSearchResult.value?.clear()
            drugAPIRetrofit.findDrugByImage(multipartBody).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        snackBarMessage.value = "Could connect to server."
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() == 200) {
                            updateDrugsList(response)
                        } else {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            snackBarMessage.value = jObjError["message"] as String
                        }
                    }
                }
            )
        } else {
            snackBarMessage.value = "Could not convert image to base64."
        }
    }

    fun searchDrugByName(drugName: String) {
        if (drugName.isNotEmpty()) {
            drugsSearchResult.value?.clear()
            drugAPIRetrofit.findDrugByName(drugName).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        snackBarMessage.value = "Could connect to server."
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() == 200) {
                            updateDrugsList(response)
                        } else {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            snackBarMessage.value = jObjError["message"] as String
                        }
                    }
                }
            )
        } else {
            snackBarMessage.value = "Please enter a valid drug name"
        }
    }

    fun getInteractionList(email: String, profileName: String, rxcui: Int) {
        drugAPIRetrofit.findInteractionList(email, profileName, rxcui.toString()).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    snackBarMessage.value = "Could not search interactions."
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        updateInteractionList(response)
                    } else {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        snackBarMessage.value = jObjError["message"] as String
                    }
                }
            }
        )
    }


    private fun updateInteractionList(response: Response<ResponseBody>) {
        var result = ""
        val drugInterBody = JSONArray(response.body()!!.string())
        for (i in 0 until drugInterBody.length()) {
            val interaction = drugInterBody.getJSONObject(i)
            result += "\n#" + (
                    interaction.get("interaction") as JSONObject).get("name") + "\nDescription:\n" + (
                    interaction.get("description") as String) + "\n"
        }

        drugsInteractionResult.value = result
    }

    private fun updateDrugsList(response: Response<ResponseBody>) {
        val drugListBody = JSONArray(response.body()!!.string())
        drugsSearchResult.value = parseDrugList(drugListBody)
        if (drugsSearchResult.value!!.size == 0) {
            snackBarMessage.value = "No drugs found!"
        }
    }

    private fun parseDrugList(drugListBody: JSONArray): MutableList<DrugOccurrence> {
        val drugs = mutableListOf<DrugOccurrence>()
        for (i in 0 until drugListBody.length()) {
            val item = drugListBody[i] as JSONArray
            for (j in 0 until item.length()) {
                val drugItem = item.get(j) as JSONObject
                drugs.add(
                    DrugOccurrence(
                        drug_name = removeParenthesis(drugItem.getString("name")),
                        rxcui = removeParenthesis(drugItem.get("rxcui").toString()).toInt()
                    )
                )
            }
        }

        return drugs
    }

    private fun removeParenthesis(data: String): String {
        return data
            .replace("[\"", "")
            .replace("\"]", "")
            .trim()
    }


}