package com.akinyele.gametableview

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

class Utils {
    companion object {
        fun dpToPx(float: Float, context: Context) : Int {
            return dpToPx(float, context.resources)
        }

        private fun dpToPx(float: Float, resources: Resources) : Int {
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, float, resources.displayMetrics)
            return px.toInt()
        }
    }
}