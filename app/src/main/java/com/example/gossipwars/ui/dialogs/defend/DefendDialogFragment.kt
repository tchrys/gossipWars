package com.example.gossipwars.ui.dialogs.defend

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.actions.AllianceInvitationDTO
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.GameHelper.camelCaseToSpaced
import com.example.gossipwars.logic.entities.GameHelper.spacedToCamelCase
import com.example.gossipwars.logic.entities.Region

class DefendDialogFragment : DialogFragment() {
    internal lateinit var listener: DefendDialogListener
    var allianceNameSelected: String? = null
    var regionNameSelected: String? = null

    interface DefendDialogListener {
        fun onDialogPositiveClick(dialog: DefendDialogResult)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DefendDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement DefendListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("activity can not be null")
        }
        val inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(activity)
        val defendProposalView: View = inflater.inflate(R.layout.alliance_defend_form, null)
        val allianceRadioGroup: RadioGroup =
            defendProposalView.findViewById(R.id.allianceDefendRadioGroup)
        val regionsRadioGroup: RadioGroup =
            defendProposalView.findViewById(R.id.regionDefendRadioGroup)

        val alliances: List<AllianceInvitationDTO>? = GameHelper.findAlliancesForPlayer(Game.myId)
            ?.map { alliance -> alliance.convertToDTO() }
        alliances?.forEach { allianceInvitationDTO: AllianceInvitationDTO ->
            val allianceButton = RadioButton(context)
            allianceButton.text = allianceInvitationDTO.name
            allianceButton.ellipsize = TextUtils.TruncateAt.END
            allianceButton.isSingleLine = true
            allianceRadioGroup.addView(allianceButton)
        }

        val regions: List<Region>? = GameHelper.findDefendableRegions()
        regions?.forEach { region: Region ->
            val regionButton = RadioButton(context)
            regionButton.text = region.name.camelCaseToSpaced()
            regionButton.ellipsize = TextUtils.TruncateAt.END
            regionButton.isSingleLine = true
            regionsRadioGroup.addView(regionButton)
        }
        if (regions?.size == 0) {
            for (i in 0..9) {
                val regionButton = RadioButton(context)
                regionButton.text = "region" + i.toString()
                regionsRadioGroup.addView(regionButton)
            }
        }

        allianceRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = allianceRadioGroup.findViewById<RadioButton>(checkedId)
            allianceNameSelected = checkedRadioButton.text.toString()
        }

        regionsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = regionsRadioGroup.findViewById<RadioButton>(checkedId)
            regionNameSelected = checkedRadioButton.text.toString().spacedToCamelCase()
        }

        builder.setView(defendProposalView)
        builder.setTitle("Select alliance and region")
            .setPositiveButton("Done") { _, _ ->
                listener.onDialogPositiveClick(
                    DefendDialogResult(
                        allianceNameSelected,
                        regionNameSelected
                    )
                )
            }
            .setNegativeButton("Cancel") { _, _ -> }
        return builder.create()
    }
}