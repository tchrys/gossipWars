package com.example.gossipwars.ui.dialogs.region

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.info.RegionPlayerInfo
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Region
import com.google.android.material.slider.Slider
import java.lang.ClassCastException
import java.lang.IllegalStateException

class RegionDialogFragment(val regionName: String): DialogFragment() {
    internal lateinit var listener: RegionDialogListener
    var regionSelected: Region? = null
    var sizeSelected: Int = 0

    interface RegionDialogListener {
        fun onDialogPositiveClick(dialog: RegionDialogResult?)
        fun onDialogNegativeClick(dialog: RegionDialogResult?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as RegionDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement RegionListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("activity can not be null")
        }
        var inflater = requireActivity().layoutInflater
        var builder = AlertDialog.Builder(activity)
        val regionView: View = inflater.inflate(R.layout.region_dialog, null)
        val dialogRegion: Region = Game.findRegionByName(regionName)!!

        val regionDescription: TextView = regionView.findViewById(R.id.regionDescription)
        var regionInfoString = ""
        regionInfoString += if (dialogRegion.occupiedBy != null) {
            "This region is occupied by " + dialogRegion.occupiedBy!!.username
        } else {
            "This region is not occupied"
        }
        regionInfoString += "\n"
        val regionPopulation: List<RegionPlayerInfo>? = Game.findRegionPopulation(regionName)
        if (regionPopulation != null && regionPopulation.isNotEmpty()) {
            regionInfoString += "Soldiers in this region: \n"
            regionPopulation.forEachIndexed { index: Int, regionPlayerInfo: RegionPlayerInfo ->
                val sizeInK: Float = 1f * regionPlayerInfo.size / 1000
                regionInfoString += "\t" + regionPlayerInfo.playerName + " : " +
                                        String.format("%.1f", sizeInK) + "k"
                if (index != regionPopulation.size - 1) {
                    regionInfoString += "\n"
                }
            }
        } else {
            regionInfoString += "There are no soldiers in this region"
        }
        regionDescription.text = regionInfoString

        val regionAttackTextView: TextView = regionView.findViewById(R.id.regionDialogAttackText)
        val attackButton: Button = regionView.findViewById(R.id.regionAttackButton)
        val canAttack: Boolean = Game.iCanAttackThisRegion(regionName)
        if (!canAttack) {
            attackButton.visibility = View.GONE
            regionAttackTextView.text = getString(R.string.cant_attack)
            regionAttackTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_warning, 0,0, 0)
        } else {
            attackButton.setOnClickListener {
                // TODO
            }
        }

        val regionsRadioGroup: RadioGroup = regionView.findViewById(R.id.neighborRegionRadioGroup)
        dialogRegion.getNeighborsList().forEach { region: Region ->
            val regionButton = RadioButton(context)
            regionButton.text = region.name
            regionsRadioGroup.addView(regionButton)
        }

        val howManySoldiers: TextView = regionView.findViewById(R.id.howManySoldiers)
        val soldiersSlider: Slider = regionView.findViewById(R.id.soldiersSlider)

        regionsRadioGroup.setOnCheckedChangeListener { _, checkedIdx ->
            val checkedRadioButton = regionsRadioGroup.findViewById<RadioButton>(checkedIdx)
            regionSelected = Game.findRegionByName(checkedRadioButton.text.toString())
            val soldiersNo: Int? = regionSelected?.name?.let { Game.soldiersForRegion(it, Game.myId) }
            if (soldiersNo == null || soldiersNo == 0) {
                soldiersSlider.visibility = View.GONE
                howManySoldiers.text = getString(R.string.no_soldiers_text)
                howManySoldiers.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_warning, 0,0, 0)
                sizeSelected = 0
            } else {
                howManySoldiers.text = getString(R.string.how_many_soldiers_to_move)
                howManySoldiers.setCompoundDrawables(null, null, null, null)
                soldiersSlider.visibility = View.VISIBLE
                soldiersSlider.valueFrom = 0.toFloat()
                soldiersSlider.valueTo = soldiersNo.toFloat()
                soldiersSlider.addOnChangeListener { _, value, _ ->
                    sizeSelected = value.toInt()
                }
            }
        }


        builder.setView(regionView)
        builder.setTitle(camelCaseToSpaced(regionName))
            .setPositiveButton("Done") { _, _ ->
                listener.onDialogPositiveClick(RegionDialogResult(regionSelected?.name, regionName, sizeSelected))
            }
            .setNegativeButton("Cancel") { _, _ ->
                listener.onDialogNegativeClick(null)
            }
        return builder.create()
    }

    fun camelCaseToSpaced(name: String): String {
        var ans: String = ""
        name.forEach { c: Char -> ans += if (c in 'A'..'Z') " " + c else c }
        return ans
    }

}