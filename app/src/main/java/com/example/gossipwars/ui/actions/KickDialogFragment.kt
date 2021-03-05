package com.example.gossipwars.ui.actions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.actions.AllianceInvitationDTO
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import java.lang.ClassCastException
import java.lang.IllegalStateException

class KickDialogFragment: DialogFragment() {
    internal lateinit var listener: KickDialogListener
    var usernameSelected: String? = null
    var allianceNameSelected: String? = null

    interface KickDialogListener {
        fun onDialogPositiveClick(dialog: KickDialogResult?)
        fun onDialogNegativeClick(dialog: KickDialogResult?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as KickDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement KickDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("activity can not be null")
        }
        var inflater = requireActivity().layoutInflater
        var builder = AlertDialog.Builder(activity)
        val kickProposalView: View = inflater.inflate(R.layout.alliance_kick_form, null)
        val allianceRadioGroup: RadioGroup = kickProposalView.findViewById(R.id.allianceKickRadioGroup)
        val playersRadioGroup: RadioGroup = kickProposalView.findViewById(R.id.playersKickRadioGroup)

        val alliances: List<AllianceInvitationDTO>? = Game.findAlliancesForPlayer(Game.myId)?.
                                                        map { alliance -> alliance.convertToDTO() }
        alliances?.forEach { allianceInvitationDTO: AllianceInvitationDTO ->
            var allianceButton = RadioButton(context)
            allianceButton.text = allianceInvitationDTO.name
            allianceRadioGroup.addView(allianceButton)
        }

        allianceRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = allianceRadioGroup.findViewById<RadioButton>(checkedId)
            allianceNameSelected = checkedRadioButton.text.toString()
            usernameSelected = null
            val players: MutableList<Player>? = Game.findPlayersInsideAlliance(checkedRadioButton.text.toString())
            playersRadioGroup.removeAllViews()
            players?.forEach { player: Player ->
                var playerButton = RadioButton(context)
                playerButton.text = player.username
                playersRadioGroup.addView(playerButton)
            }
            if (playersRadioGroup.childCount == 0) {
                for (i in 0..7) {
                    val dummyButton = RadioButton(context)
                    dummyButton.text = "player" + i.toString()
                    playersRadioGroup.addView(dummyButton)
                }
            }
            playersRadioGroup.clearCheck()
        }

        playersRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                val checkedRadioButton = playersRadioGroup.findViewById<RadioButton>(checkedId)
                if (checkedRadioButton.isChecked) {
                    usernameSelected = checkedRadioButton.text.toString()
                }
            } else {
                usernameSelected == null
            }
        }

        builder.setView(kickProposalView)
        builder.setTitle("Select alliance and player to kick")
            .setPositiveButton("Done") { _, _  ->
                listener.onDialogPositiveClick(KickDialogResult(allianceNameSelected, usernameSelected))
            }
            .setNegativeButton("Cancel") { _, _ ->
                listener.onDialogNegativeClick(null)
            }
        return builder.create()
    }

}