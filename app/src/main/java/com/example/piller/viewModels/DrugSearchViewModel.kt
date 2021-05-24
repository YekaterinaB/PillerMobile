package com.example.piller.viewModels

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.piller.api.DrugAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.DrugObject
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.JSONMessageExtractor
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
    private val _drugAPIRetrofit = ServiceBuilder.buildService(DrugAPI::class.java)

    val drugsInteractionResult: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //  this will hold the name that the user searched but no drug was found
    val drugSearchNoResult: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val drugsSearchResult: MutableLiveData<MutableList<DrugObject>> by lazy {
        MutableLiveData<MutableList<DrugObject>>()
    }

    val snackBarMessage: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val newDrug: MutableLiveData<DrugObject> by lazy { MutableLiveData<DrugObject>() }

    val addedDrugSuccess: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }

    val showLoadingScreen: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }

    lateinit var calendarId: String

    fun getDrugByRxcui(rxcui: Int): DrugObject? {
        val filteredArray = drugsSearchResult.value?.filter { drug -> drug.rxcui == rxcui }
        if (filteredArray != null && filteredArray.isNotEmpty()) {
            return filteredArray[0]
        }
        return null
    }

    private fun getFileAsMultiPart(imageFile: File): MultipartBody.Part {
        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse(DbConstants.multipartHeader), imageFile
        )

        return MultipartBody.Part.createFormData(
            DbConstants.multipartFileName,
            imageFile.name,
            requestFile
        )
    }

    fun searchDrugByPillImage(imageFilePath: String) {
        val imageFile = File(imageFilePath)

        if (imageFile.exists() && imageFile.isFile) {
            showLoadingScreen.value = true
            val multipartBody = getFileAsMultiPart(imageFile)
            drugsSearchResult.value?.clear()
            _drugAPIRetrofit.findDrugByImage(multipartBody).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        snackBarMessage.value = DbConstants.couldNotConnectServerError
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() == DbConstants.OKCode) {
                            updateDrugsList(response)
                        } else {
                            snackBarMessage.value = JSONMessageExtractor.getErrorMessage(response)
                        }
                        showLoadingScreen.value = false
                    }
                }
            )
        } else {
            snackBarMessage.value = DbConstants.convertingToBase64Error
        }
    }

    fun searchDrugByName(drugName: String) {
        if (drugName.isNotEmpty()) {
            showLoadingScreen.value = true
            drugsSearchResult.value?.clear()
            _drugAPIRetrofit.findDrugByName(drugName).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        snackBarMessage.value = DbConstants.couldNotConnectServerError
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() == DbConstants.OKCode) {
                            updateDrugsList(response)
                        } else {
                            snackBarMessage.value = JSONMessageExtractor.getErrorMessage(response)
                        }
                        showLoadingScreen.value = false
                    }
                }
            )
        } else {
            snackBarMessage.value = DbConstants.invalidDrugNameError
        }
    }

    fun getInteractionList(loggedUserObject: UserObject, rxcui: Int) {
        showLoadingScreen.value = true
        _drugAPIRetrofit.findInteractionList(
            loggedUserObject.userId,
            loggedUserObject.currentProfile!!.profileId,
            rxcui.toString()
        ).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    snackBarMessage.value = DbConstants.unableToSearchInteractionsError
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == DbConstants.OKCode) {
                        updateInteractionList(response)
                    } else {
                        snackBarMessage.value = JSONMessageExtractor.getErrorMessage(response)
                    }
                    showLoadingScreen.value = false
                }
            }
        )
    }


    private fun updateInteractionList(response: Response<ResponseBody>) {
        var result = ""
        val drugInterBody = JSONArray(response.body()!!.string())
        for (i in 0 until drugInterBody.length()) {
            val interaction = drugInterBody.getJSONObject(i)
            val str = SpannableStringBuilder(
                "<br><b>#" + (
                        interaction.get(DbConstants.interaction) as JSONObject).get(DbConstants.drugName) + "</b><br><u>Description:</u><br>" + (
                        interaction.get(DbConstants.description) as String) + "<br>"
            )
            str.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                str.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            result += str
        }

        drugsInteractionResult.value = result
    }

    private fun updateDrugsList(response: Response<ResponseBody>) {
        val drugListBody = JSONArray(response.body()!!.string())
        drugsSearchResult.value = parseDrugList(drugListBody)
        if (drugsSearchResult.value!!.size == 0) {
            snackBarMessage.value = DbConstants.noDrugsFoundMessage
        }
    }

    private fun parseDrugList(drugListBody: JSONArray): MutableList<DrugObject> {
        val drugs = mutableListOf<DrugObject>()
        for (i in 0 until drugListBody.length()) {
            val item = drugListBody[i] as JSONArray
            for (j in 0 until item.length()) {
                val drugItem = item.get(j) as JSONObject
                drugs.add(
                    DrugObject(
                        DbConstants.defaultStringValue, //  drugid is empty because it's search result and we didn't save it in db
                        calendarId,
                        drugName = removeParenthesis(drugItem.getString(DbConstants.drugName)),
                        rxcui = removeParenthesis(
                            drugItem.get(DbConstants.rxcui).toString()
                        ).toInt()
                    )
                )
            }
        }

        return drugs
    }

    private fun removeParenthesis(data: String): String {
        return data
            .replace("[\"", DbConstants.defaultStringValue)
            .replace("\"]", DbConstants.defaultStringValue)
            .trim()
    }

    fun searchDrugByBox(filePath: String) {
        val imageFile = File(filePath)
        if (imageFile.exists() && imageFile.isFile) {
            showLoadingScreen.value = true
            val multipartBody = getFileAsMultiPart(imageFile)
            drugsSearchResult.value?.clear()
            _drugAPIRetrofit.findDrugByBox(multipartBody).enqueue(
                object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        snackBarMessage.value = DbConstants.couldNotConnectServerError
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.raw().code() == DbConstants.OKCode) {
                            updateDrugsList(response)
                        } else {
                            snackBarMessage.value = JSONMessageExtractor.getErrorMessage(response)
                        }
                        showLoadingScreen.value = false
                    }
                }
            )
        }
    }
}