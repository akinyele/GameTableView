package com.akinyele.gametableviewsample


import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.akinyele.gametableview.GameTableView
import com.akinyele.gametableview.Utils
import com.thebluealliance.spectrum.SpectrumDialog
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.item_table_background.*
import kotlinx.android.synthetic.main.item_table_border_options.*
import kotlinx.android.synthetic.main.item_table_corner_options.*
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = this::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBottomSheetViewOptions()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }


    private fun setBottomSheetViewOptions() {

        //Table background Options
        elevationSeekBar.max = 24
        elevationSeekBar.setOnProgressChangeListener(progressChangeListener)
        backgroundColorImageView.setOnClickListener {
            showColorPicker(gameTableView.mTableBackGroundColor, backgroundColorImageView)
        }

        // Table Border options
        borderWithSeekBar.max = 20
        borderWithSeekBar.setOnProgressChangeListener(progressChangeListener)
        borderEnableCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            gameTableView.mShowBorder = isChecked
        }
        borderColorImageView.setOnClickListener {
            showColorPicker(
                gameTableView.mTableBorderColor,
                borderColorImageView
            )
        }


        // Table corner options
        val tableCornersAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.table_corners,
            android.R.layout.simple_dropdown_item_1line
        )
        cornerRadiusSeekBar.setOnProgressChangeListener(progressChangeListener)
        cornerTypeSpinner.adapter = tableCornersAdapter
        cornerTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedType = parent?.getItemAtPosition(position).toString()
                when (selectedType) {
                    "Normal" -> gameTableView.mTableType = GameTableView.TableType.NORMAL
                    "Rounded" -> gameTableView.mTableType = GameTableView.TableType.ROUNDED
                    "Scallop" -> gameTableView.mTableType = GameTableView.TableType.SCALLOP
                    "Edge" -> gameTableView.mTableType = GameTableView.TableType.EDGE
                    else -> {
                        //do nothing
                    }
                }
            }
        }
    }


    private fun showColorPicker(selectedColor: Int, colorView: ImageView) {
        SpectrumDialog.Builder(this)
            .setColors(R.array.colors)
            .setSelectedColor(selectedColor)
            .setDismissOnColorSelected(true)
            .setOutlineWidth(1)
            .setOnColorSelectedListener { positiveResult, color ->
                Log.w(TAG, "Color selected $color")
                if (positiveResult) {
                    colorView.background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

                    when (colorView.id) {
                        R.id.borderColorImageView -> gameTableView.mTableBorderColor = color
                        R.id.backgroundColorImageView -> gameTableView.mTableBackGroundColor = color
                        else -> {
                            //do nothing
                        }
                    }
                }
            }.build().show(supportFragmentManager, "ColorPicker")
    }


    private var progressChangeListener = object : DiscreteSeekBar.OnProgressChangeListener {
        override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {

            Log.w(TAG, "SeekBar progress changes $value $seekBar")

            val valueInPx = Utils.dpToPx(value.toFloat(), this@MainActivity)
            when (seekBar?.id) {
                R.id.elevationSeekBar -> gameTableView.mTableElevation = valueInPx.toFloat()
                R.id.borderWithSeekBar -> gameTableView.mTableBorderWidth = valueInPx
                R.id.cornerRadiusSeekBar -> gameTableView.mCornerRadius = valueInPx
            }
        }

        override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
        }
    }

}
