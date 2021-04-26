package com.example.gossipwars.ui.dialogs.join

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Transformations.map
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.actions.AllianceInvitationDTO
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.Player
import java.lang.IllegalStateException
import java.util.Locale.filter

class JoinDialogFragment : DialogFragment() {
    internal lateinit var listener: JoinDialogListener
    var usernameSelected: String? = null
    var allianceNameSelected: String? = null

    interface JoinDialogListener {
        fun onDialogPositiveClick(dialog: JoinDialogResult)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as JoinDialogListener
        } catch (e: ClassCastException) {
            throw java.lang.ClassCastException(context.toString() + " must implement JoinListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("activity can not be null")
        }
        val inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(activity)
        val joinProposalView: View = inflater.inflate(R.layout.alliance_join_form, null)
        val allianceRadioGroup: RadioGroup =
            joinProposalView.findViewById(R.id.allianceJoinRadioGroup)
        val playersRadioGroup: RadioGroup =
            joinProposalView.findViewById(R.id.playersJoinRadioGroup)

        val alliances: List<AllianceInvitationDTO>? = GameHelper.findAllianceWithJoinOption(Game.myId)
            ?.map { alliance -> alliance.convertToDTO() }
        alliances?.forEach { allianceInvitationDTO: AllianceInvitationDTO ->
            val allianceButton = RadioButton(context)
            allianceButton.text = allianceInvitationDTO.name
            allianceButton.ellipsize = TextUtils.TruncateAt.END
            allianceButton.isSingleLine = true
            allianceRadioGroup.addView(allianceButton)
        }

        allianceRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = allianceRadioGroup.findViewById<RadioButton>(checkedId)
            allianceNameSelected = checkedRadioButton.text.toString()
            usernameSelected = null
            val players: MutableList<Player>? =
                GameHelper.findPlayersOutsideAlliance(checkedRadioButton.text.toString())
            playersRadioGroup.removeAllViews()
            players?.forEach { player: Player ->
                var playerButton = RadioButton(context)
                playerButton.text = player.username
                playerButton.ellipsize = TextUtils.TruncateAt.END
                playerButton.isSingleLine = true
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
                usernameSelected = null
            }
        }

        builder.setView(joinProposalView)
        builder.setTitle("Select alliance and player to join")
            .setPositiveButton("Done") { _, _ ->
                listener.onDialogPositiveClick(
                    JoinDialogResult(
                        allianceNameSelected,
                        usernameSelected
                    )
                )
            }
            .setNegativeButton("Cancel") { _, _ -> }
        return builder.create()
    }

}