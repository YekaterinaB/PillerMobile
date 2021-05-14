package com.example.piller.customWidgets

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import com.example.piller.R
import com.example.piller.utilities.DbConstants


class NumberPickerMinMax : NumberPicker {
    private var _stringItems: Array<String>? = null

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs) {
        processAttributeSet(attrs)
    }

    constructor (context: Context?, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        processAttributeSet(attrs)
    }

    private fun processAttributeSet(attrs: AttributeSet) {
        //This method reads the parameters given in the xml file and sets the properties according to it
        val stringAttrs = attrs.getAttributeValue(null, DbConstants.stringArgsName)
        var minValue = DbConstants.minNumberPickerValue
        val maxValue: Int
        if (stringAttrs != null) {
            this.displayedValues = stringAttrs.split(',').toTypedArray()
            maxValue = this.displayedValues.size - 1
        } else {
            minValue = attrs.getAttributeIntValue(
                null,
                DbConstants.minAttribute,
                DbConstants.defaultNumberPickerValue
            )
            maxValue = attrs.getAttributeIntValue(
                null,
                DbConstants.maxAttribute,
                DbConstants.defaultNumberPickerValue
            )
        }
        _stringItems = this.displayedValues
        this.minValue = minValue
        this.maxValue = maxValue
        setNumberPickerTextColor(R.color.colorPrimary)
        this.wrapSelectorWheel = false
        this.value = attrs.getAttributeIntValue(null, DbConstants.valueAttribute, this.minValue)
    }

    fun getItemAt(index: Int): String {
        var item = ""
        if (index < this._stringItems?.size!!) {
            item = this._stringItems!![index]
        }

        return item
    }

    fun getSelectedItemString(): String? {
        return _stringItems?.get(this.value)
    }

    private fun setNumberPickerTextColor(color: Int) {
        try {
            val selectorWheelPaintField =
                this.javaClass.getDeclaredField(DbConstants.selectorDeclaredField)
            selectorWheelPaintField.isAccessible = true
            (selectorWheelPaintField.get(this) as Paint).color = color
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        val count = this.childCount
        for (i in 0 until count) {
            val child: View = this.getChildAt(i)
            if (child is EditText) child.setTextColor(color)
        }
        this.invalidate()
    }
}