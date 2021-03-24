package com.example.piller

import com.example.piller.models.DrugObject

class DrugMap {
    private val drugCache: MutableMap<String, MutableMap<String, DrugObject>> = mutableMapOf()

    fun getDrugObject(calendarID: String, drugID: String): DrugObject {
        return drugCache[calendarID]?.get(drugID)!!
    }

    fun setDrugObject(calendarID: String, drugObject: DrugObject) {
        if (drugCache[calendarID] == null) {
            drugCache[calendarID] = mutableMapOf()
        }
        drugCache[calendarID]!![drugObject.drugId] = drugObject
    }

    fun removeDrugFromMap(calendarID: String, drugObject: DrugObject) {
        drugCache[calendarID]?.remove(drugObject.drugId)
    }


    companion object {
        val instance = DrugMap()
    }
}