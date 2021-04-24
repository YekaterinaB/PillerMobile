package com.example.piller.customWidgets

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import com.example.piller.R


class NumberPickerMinMax : NumberPicker {
    private var stringItems: Array<String>? = null

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
        val stringAttrs = attrs.getAttributeValue(null, "values")
        var minValue = 0
        val maxValue: Int
        if (stringAttrs != null) {
            this.displayedValues = stringAttrs.split(',').toTypedArray()
            maxValue = this.displayedValues.size - 1
        } else {
            minValue = attrs.getAttributeIntValue(null, "min", 0)
            maxValue = attrs.getAttributeIntValue(null, "max", 0)
        }
        stringItems = this.displayedValues
        this.minValue = minValue
        this.maxValue = maxValue
        setNumberPickerTextColor(R.color.colorPrimary)
        this.wrapSelectorWheel = false
        this.value = attrs.getAttributeIntValue(null, "value", this.minValue)
    }

    fun getItemAt(index: Int): String {
        var item = ""
        if (index < this.stringItems?.size!!) {
            item = this.stringItems!![index]
        }

        return item
    }

    fun getSelectedItemString(): String? {
        return stringItems?.get(this.value)
    }

    private fun setNumberPickerTextColor(color: Int) {
        try {
            val selectorWheelPaintField = this.javaClass
                .getDeclaredField("mSelectorWheelPaint")
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