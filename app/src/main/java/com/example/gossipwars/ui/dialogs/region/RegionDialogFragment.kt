package com.example.gossipwars.ui.dialogs.region

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.actions.StrategyActionDTO
import com.example.gossipwars.communication.messages.info.RegionPlayerInfo
import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.GameHelper.camelCaseToSpaced
import com.example.gossipwars.logic.entities.GameHelper.spacedToCamelCase
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.entities.Region
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.google.android.material.slider.Slider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class RegionDialogFragment(private val regionName: String) : DialogFragment() {
    internal lateinit var listener: RegionDialogListener
    var regionSelected: Region? = null
    var sizeSelected: Int = 0

    interface RegionDialogListener {
        fun onDialogPositiveClick(dialog: RegionDialogResult)
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
        val inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(activity)
        val regionView: View = inflater.inflate(R.layout.region_dialog, null)
        val dialogRegion: Region = GameHelper.findRegionByName(regionName)!!

        val regionDescription: TextView = regionView.findViewById(R.id.regionDescription)
        var regionInfoString = ""
        regionInfoString += if (dialogRegion.occupiedBy != null) {
            "This region is occupied by ${dialogRegion.occupiedBy!!.username}"
        } else {
            "This region is not occupied"
        }
        regionInfoString += "\n"
        val regionPopulation: List<RegionPlayerInfo>? = GameHelper.findRegionPopulation(regionName)
        if (regionPopulation != null && regionPopulation.isNotEmpty()) {
            regionInfoString += "Soldiers in this region: \n"
            regionPopulation.forEachIndexed { index: Int, regionPlayerInfo: RegionPlayerInfo ->
                val sizeInK: Float = 1f * regionPlayerInfo.size / 1000
                regionInfoString += "\t${regionPlayerInfo.playerName} : " +
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
        val canAttack: Boolean = GameHelper.iCanAttackThisRegion(regionName)
        if (!canAttack) {
            attackButton.visibility = View.GONE
            regionAttackTextView.text = getString(R.string.cant_attack)
            regionAttackTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_warning, 0, 0, 0
            )
        } else {
            attackButton.setOnClickListener {
                val targetId: UUID? = dialogRegion.occupiedBy?.id
                if (targetId != null) {
                    Game.sendStrategyAction(
                        StrategyAction(
                            GameHelper.findPlayerByUUID(Game.myId),
                            GameHelper.findPlayerByUUID(targetId),
                            dialogRegion.id, mutableListOf(), ProposalEnum.ATTACK
                        )
                    )
                }
            }
        }
        val meAsAPlayer: Player = GameHelper.findPlayerByUUID(Game.myId)
        val regionsRadioGroup: RadioGroup = regionView.findViewById(R.id.neighborRegionRadioGroup)
        dialogRegion.getNeighborsList().filter { region ->
            GameHelper.canMoveFromThisRegion(region)
        }.forEach { region: Region ->
            val regionButton = RadioButton(context)
            regionButton.text = region.name.camelCaseToSpaced()
            regionsRadioGroup.addView(regionButton)
        }
        val soldiersSlider: Slider = regionView.findViewById(R.id.soldiersSlider)
        val howManySoldiers: TextView = regionView.findViewById(R.id.howManySoldiers)
        if (regionsRadioGroup.childCount == 0) {
            regionsRadioGroup.visibility = View.GONE
            howManySoldiers.text = getString(R.string.no_soldiers_in_neighbors)
            howManySoldiers.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_warning, 0, 0, 0
            )
            soldiersSlider.visibility = View.GONE
        } else {
            regionsRadioGroup.setOnCheckedChangeListener { _, checkedIdx ->
                sizeSelected = 0
                soldiersSlider.value = 0.toFloat()
                val checkedRadioButton = regionsRadioGroup.findViewById<RadioButton>(checkedIdx)
                regionSelected = GameHelper.findRegionByName(
                    checkedRadioButton.text.toString().spacedToCamelCase()
                )
                val soldiersNo: Int? =
                    regionSelected?.name?.let { GameHelper.soldiersForRegion(it, Game.myId) }
                howManySoldiers.text = getString(R.string.how_many_soldiers_to_move)
                howManySoldiers.setCompoundDrawables(null, null, null, null)
                soldiersSlider.visibility = View.VISIBLE
                soldiersSlider.valueFrom = 0.toFloat()
                if (soldiersNo != null) {
                    soldiersSlider.valueTo = soldiersNo.toFloat()
                    soldiersSlider.stepSize = getStepSize(soldiersSlider.valueTo.toInt())
                }
                val soldiersAlreadyUsed: Int? =
                    meAsAPlayer.soldiersUsedThisRound[regionSelected?.id]
                if (soldiersAlreadyUsed != null) {
                    soldiersSlider.valueTo -= soldiersAlreadyUsed
                }
                soldiersSlider.addOnChangeListener { _, value, _ ->
                    sizeSelected = value.toInt()
                }
            }
        }


        builder.setView(regionView)
        builder.setTitle(regionName.camelCaseToSpaced())
            .setPositiveButton("Done") { _, _ ->
                listener.onDialogPositiveClick(
                    RegionDialogResult(
                        regionSelected?.name,
                        regionName,
                        sizeSelected
                    )
                )
            }
            .setNegativeButton("Cancel") { _, _ -> }
        return builder.create()
    }

    fun Int?.isGreaterThan(other: Int) = this != null && this > other

    fun getStepSize(valueTo: Int): Float {
        if (valueTo < 1000)
            return 1f
        if (valueTo % 100 == 0) {
            return (valueTo / 100).toFloat()
        } else if (valueTo % 10 == 0) {
            return (valueTo / 10).toFloat()
        }
        return 1f
    }


}