package com.example.piller.utilities

import com.example.piller.models.DrugObject
import com.example.piller.models.Occurrence
import org.json.JSONArray
import org.json.JSONObject

class parserUtils{
    companion object {
         fun parsedDrugObject(drug: JSONObject, intakeDates: JSONObject,calendarId:String): DrugObject {
            val takenId=intakeDates.get("taken_id").toString()
            val occurrenceObject= drug.get("occurrence") as JSONObject
            val drugOccur  = parseOccurrenceObject(occurrenceObject)
            val drugObj = DrugObject(calendarId,
                drug.get("name") as String,
                drug.get("rxcui").toString().toInt(),
                takenId, drugOccur
            )
            return drugObj
        }

         fun parseOccurrenceObject(occurrenceObject: JSONObject) : Occurrence {
            val occurrence= Occurrence()
            occurrence.event_id=occurrenceObject.get("event_id") as String
            val drugInfo=occurrenceObject.get("drug_info") as JSONObject
            occurrence.repeatStart = (drugInfo.get("repeat_start") as String).toLong()
            occurrence.repeatEnd = (drugInfo.get("repeat_end") as String).toLong()
            occurrence.repeatYear = drugInfo.get("repeat_year") as Int
            occurrence.repeatMonth = drugInfo.get("repeat_month") as Int
            occurrence.repeatDay = drugInfo.get("repeat_day") as Int
            occurrence.repeatWeek = drugInfo.get("repeat_week") as Int
            occurrence.repeatWeekday =
                parseRepeatWeekdayArray(drugInfo.get("repeat_weekday")as JSONArray)

            return occurrence
        }

        private fun parseRepeatWeekdayArray(weekdayJson: JSONArray):List<Int>{
            val intList= mutableListOf<Int>()
            for(i in 0 until weekdayJson.length()){
                intList.add(weekdayJson.optInt(i))
            }
            return intList
        }

    }
}