package com.example.piller

import com.example.piller.models.DrugObject

class DrugMap {
    private val _drugCache: MutableMap<String, MutableMap<String, DrugObject>> = mutableMapOf()

    fun getDrugObject(calendarID: String, drugID: String): DrugObject {
        return _drugCache[calendarID]?.get(drugID)!!
    }

    fun setDrugObject(calendarID: String, drugObject: DrugObject) {
        if (_drugCache[calendarID] == null) {
            _drugCache[calendarID] = mutableMapOf()
        }
        _drugCache[calendarID]!![drugObject.drugId] = drugObject
    }

    fun removeDrugFromMap(calendarID: String, drugObject: DrugObject) {
        _drugCache[calendarID]?.remove(drugObject.drugId)
    }


    companion object {
        val instance = DrugMap()
    }
}