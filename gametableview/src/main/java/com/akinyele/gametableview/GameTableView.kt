package com.akinyele.gametableview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config.ALPHA_8
import android.graphics.Color.BLACK
import android.graphics.Color.TRANSPARENT
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PorterDuff.Mode.SRC_IN
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.IntDef

class GameTableView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    companion object {
        val TAG = this::class.simpleName
        private const val MAX_ELEVATION = 24f
    }


    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TableType.NORMAL, TableType.ROUNDED, TableType.SCALLOP, TableType.EDGE)
    annotation class TableType {
        companion object {
            const val NORMAL = 0
            const val ROUNDED = 1
            const val SCALLOP = 2
            const val EDGE = 3
        }
    }

    private val mPath = Path()

    private val mTableBorderPaint = Paint()
    private val mTableBackgroundPaint = Paint()
    private var mRoundedCornerArc = RectF()
    private var mScallopCornerArc = RectF()
    private var mEdgeCornerClip = RectF()

    var mTableBackGroundColor = 0
        set(value) {
            field = value
            initElements()
        }

    var mTableBorderColor = 0
        set(value) {
            field = value
            initElements()
        }
    var mTableBorderWidth = 0
        set(value) {
            field = value
            initElements()
        }
    var mShowBorder = true
        set(value) {
            field = value
            initElements()
        }
    var mTableType = TableType.ROUNDED
        set(value) {
            field = value
            initElements()
        }
    var mCornerRadius = 0
        set(value) {
            field = value

            initElements()
        }

    var mTableElevation = 0f
        set(value) {
            field = value
            Log.w(TAG,"Elevation value updated $value")
            setShadowBlur(value)
            initElements()
        }

    private var mShadow: Bitmap? = null
    private val mShadowPaint = Paint(ANTI_ALIAS_FLAG)
    private var mShadowBlurRadius = 0f


    init {
        if (attrs != null) {
            // initialize attributes
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GameTableView)
            mCornerRadius = typedArray.getDimensionPixelSize(R.styleable.GameTableView_tableCornerRadius, Utils.dpToPx(0f, context))
            mTableType = typedArray.getInt(R.styleable.GameTableView_tableType, TableType.NORMAL)
            mTableBackGroundColor = typedArray.getColor(
                R.styleable.GameTableView_tableBackgroundColor,
                resources.getColor(android.R.color.white)
            )
            mTableBorderColor = typedArray.getColor(
                R.styleable.GameTableView_tableBorderColor,
                resources.getColor(android.R.color.black)
            )
            mTableBorderWidth = typedArray.getDimensionPixelSize(R.styleable.GameTableView_tableBorderWidth, 0)
            mShowBorder = typedArray.getBoolean(R.styleable.GameTableView_tableShowBorder, true)

            var mTableElevation = 0f
            if (typedArray.hasValue(R.styleable.GameTableView_tableElevation)) {
                mTableElevation = typedArray.getDimension(R.styleable.GameTableView_tableElevation, 0f)
            } else if (typedArray.hasValue(R.styleable.GameTableView_android_elevation)) {
                mTableElevation = typedArray.getDimension(R.styleable.GameTableView_android_elevation, 0f)
            }

            if (mTableElevation > 0f) {
                // set up table shadow
                setShadowBlur(mTableElevation)
            }

            typedArray.recycle()

            mShadowPaint.colorFilter = PorterDuffColorFilter(BLACK, SRC_IN)
            mShadowPaint.alpha = 51 // 20%
            initElements()

            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun initElements() {
        setBackgroundPaint()
        setBorderPaint()

        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        layoutTable()

        if (mShadowBlurRadius > 0f && !isInEditMode) canvas?.drawBitmap(mShadow!!, 0f, mShadowBlurRadius / 1.5f, null)

        canvas?.drawPath(mPath, mTableBackgroundPaint)
        if (mShowBorder) canvas?.drawPath(mPath, mTableBorderPaint)
    }

    private fun layoutTable() {

        // setup positions
        val right = width - paddingRight - mShadowBlurRadius - if (mShowBorder) mTableBorderWidth.div(2) else 0
        val left = paddingLeft + mShadowBlurRadius + if (mShowBorder) mTableBorderWidth.div(2) else 0
        val top = paddingTop + mShadowBlurRadius.div(2) + if (mShowBorder) mTableBorderWidth.div(2) else 0
        val bottom = height - mShadowBlurRadius - mShadowBlurRadius.div(2) - if (mShowBorder) mTableBorderWidth.div(2) else 0
        mPath.reset()

        // Check corner radius

        // setup able depending on the table type
        when (mTableType) {
            TableType.ROUNDED -> {
                mPath.arcTo(getTopLeftCorner(left, top), 180f, 90f)
                mPath.lineTo(left + mCornerRadius, top)

                mPath.lineTo(right - mCornerRadius, top)
                mPath.arcTo(getTopRightCornerArc(right, top), 270f, 90f)

                mPath.lineTo(right, bottom - mCornerRadius)
                mPath.arcTo(getBottomRightCornerArc(right, bottom), 0f, 90f)

                mPath.lineTo(left + mCornerRadius, bottom)
                mPath.arcTo(getBottomLeftCorner(left, bottom), 90f, 90f)
            }
            TableType.SCALLOP -> {
                mPath.arcTo(getScallopTopLeftCorner(left, top), 90f, -90f)
                mPath.lineTo(left + mCornerRadius, top)

                mPath.lineTo(right - mCornerRadius, top)
                mPath.arcTo(getScallopTopRightCornerArc(right, top), 180f, -90f)

                mPath.lineTo(right, bottom - mCornerRadius)
                mPath.arcTo(getScallopBottomRightCornerArc(right, bottom), 270f, -90f)

                mPath.lineTo(left + mCornerRadius, bottom)
                mPath.arcTo(getScallopBottomLeftCorner(left, bottom), 0f, -90f)
            }
            TableType.EDGE -> {
                mPath.moveTo(left, top + mCornerRadius)
                mPath.lineTo(left + mCornerRadius, top)

                mPath.lineTo(right - mCornerRadius, top)
                mPath.lineTo(right, top + mCornerRadius)

                mPath.lineTo(right, bottom - mCornerRadius)
                mPath.lineTo(right - mCornerRadius, bottom)

                mPath.lineTo(left + mCornerRadius, bottom)
                mPath.lineTo(left, bottom - mCornerRadius)
            }
            //TableType.NORMAL -> { }
            else -> {
                mPath.moveTo(left, top)
                mPath.lineTo(right, top)

                mPath.lineTo(right, bottom)
                mPath.lineTo(left, bottom)
            }
        }
        mPath.close()

        generateShadow()
    }


// region helper methods

    private fun setBackgroundPaint() {
        mTableBackgroundPaint.alpha = 0
        mTableBackgroundPaint.isAntiAlias = true
        mTableBackgroundPaint.color = mTableBackGroundColor
        mTableBackgroundPaint.style = Paint.Style.FILL
    }

    private fun setBorderPaint() {
        mTableBorderPaint.alpha = 0
        mTableBorderPaint.isAntiAlias = true
        mTableBorderPaint.color = mTableBorderColor
        mTableBorderPaint.strokeWidth = mTableBorderWidth.toFloat()
        mTableBorderPaint.style = Paint.Style.STROKE
    }

    private fun generateShadow() {
        if (isJellyBeanAndAbove() && !isInEditMode) {
            if (mShadowBlurRadius == 0f) return

            if (mShadow == null) {
                mShadow = Bitmap.createBitmap(width, height, ALPHA_8)
            } else {
                mShadow!!.eraseColor(TRANSPARENT)
            }
            val c = Canvas(mShadow)
            c.drawPath(mPath, mShadowPaint)
            if (mShowBorder) {
                c.drawPath(mPath, mShadowPaint)
            }
            val rs = RenderScript.create(context)
            val blur = ScriptIntrinsicBlur.create(rs, Element.U8(rs))
            val input = Allocation.createFromBitmap(rs, mShadow)
            val output = Allocation.createTyped(rs, input.type)
            blur.setRadius(mShadowBlurRadius)
            blur.setInput(input)
            blur.forEach(output)
            output.copyTo(mShadow)
            input.destroy()
            output.destroy()
            blur.destroy()
        }
    }

    private fun getTopLeftCorner(left: Float, top: Float): RectF {
        mRoundedCornerArc.set(left, top, left + mCornerRadius.times(2), top + mCornerRadius.times(2))
        return mRoundedCornerArc
    }

    private fun getTopRightCornerArc(right: Float, top: Float): RectF {
        mRoundedCornerArc.set(right - mCornerRadius.times(2), top, right, top + mCornerRadius.times(2))
        return mRoundedCornerArc
    }

    private fun getBottomLeftCorner(left: Float, bottom: Float): RectF {
        mRoundedCornerArc.set(left, bottom - mCornerRadius.times(2), left + mCornerRadius.times(2), bottom)
        return mRoundedCornerArc
    }

    private fun getBottomRightCornerArc(right: Float, bottom: Float): RectF {
        mRoundedCornerArc.set(right - mCornerRadius.times(2), bottom - mCornerRadius.times(2), right, bottom)
        return mRoundedCornerArc
    }

    private fun getScallopTopLeftCorner(left: Float, top: Float): RectF {
        mScallopCornerArc.set(left - mCornerRadius, top- mCornerRadius, left + mCornerRadius, top + mCornerRadius)
        return mScallopCornerArc
    }

    private fun getScallopTopRightCornerArc(right: Float, top: Float): RectF {
        mScallopCornerArc.set(right - mCornerRadius, top - mCornerRadius, right + mCornerRadius, top + mCornerRadius)
        return mScallopCornerArc
    }

    private fun getScallopBottomLeftCorner(left: Float, bottom: Float): RectF {
        mScallopCornerArc.set(left - mCornerRadius, bottom - mCornerRadius, left + mCornerRadius, bottom + mCornerRadius)
        return mScallopCornerArc
    }

    private fun getScallopBottomRightCornerArc(right: Float, bottom: Float): RectF {
        mScallopCornerArc.set(right - mCornerRadius, bottom - mCornerRadius, right + mCornerRadius, bottom + mCornerRadius)
        return mScallopCornerArc
    }

    private fun getEdgeTopLeftCorner(left: Float, top: Float): RectF {
        mRoundedCornerArc.set(left, top, left + mCornerRadius.times(2), top + mCornerRadius.times(2))
        return mRoundedCornerArc
    }

    private fun getEdgeTopRightCornerArc(right: Float, top: Float): RectF {
        mRoundedCornerArc.set(right - mCornerRadius.times(2), top, right, top + mCornerRadius.times(2))
        return mRoundedCornerArc
    }

    private fun getEdgeBottomLeftCorner(left: Float, bottom: Float): RectF {
        mRoundedCornerArc.set(left, bottom - mCornerRadius.times(2), left + mCornerRadius.times(2), bottom)
        return mRoundedCornerArc
    }

    private fun getEdgeBottomRightCornerArc(right: Float, bottom: Float): RectF {
        mRoundedCornerArc.set(right - mCornerRadius.times(2), bottom - mCornerRadius.times(2), right, bottom)
        return mRoundedCornerArc
    }


    private fun setShadowBlur(elevation: Float) {
        if (!isJellyBeanAndAbove()) {
            Log.w(TAG, "Table elevation only work on Android Jellybean and above")
        }

        // Shadow will always have a max elevation of 25dp
        val maxElevation = Utils.dpToPx(MAX_ELEVATION, context).toFloat()
        mShadowBlurRadius = Math.min(25f * elevation.div(maxElevation), 25f)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun isJellyBeanAndAbove(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
    }
// endregion


}