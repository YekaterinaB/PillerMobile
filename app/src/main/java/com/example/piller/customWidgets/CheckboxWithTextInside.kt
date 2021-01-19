package com.example.piller.customWidgets

import android.content.Context
import android.util.AttributeSet
import com.example.piller.R


class CheckboxWithTextInside(context: Context?, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatCheckBox(context, attrs) {
    override fun setChecked(t: Boolean) {
        if (t) {
            setBackgroundResource(R.drawable.checkbox_select)
        } else {
            setBackgroundResource(R.drawable.checkbox_deselct)
        }
        super.setChecked(t)
    }
}