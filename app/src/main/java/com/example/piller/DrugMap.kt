package com.example.piller

import com.example.piller.models.DrugObject

class DrugMap {
    private val drugCache: MutableMap<String, MutableMap<String, DrugObject>> = mutableMapOf()

    fun getDrugObject(calendarID: String, drugID: String): DrugObject {
        return drugCache[calendarID]?.get(drugID)!!
    }

    fun setDrugObject(calendarID: String, drugObject: DrugObject) {
        val drugID = drugObject.drugName + drugObject.rxcui.toString()
        if (drugCache[calendarID] == null) {
            drugCache[calendarID] = mutableMapOf()
        }
        drugCache[calendarID]!!.put(drugID, drugObject)
    }

    fun removeDrugFromMap(calendarID: String, drugObject: DrugObject) {
        val drugID = drugObject.drugName + drugObject.rxcui.toString()

        drugCache[calendarID]?.remove(drugID)
    }


    companion object {
        val instance = DrugMap()
    }
}