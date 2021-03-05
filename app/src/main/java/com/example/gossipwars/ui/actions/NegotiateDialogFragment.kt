package com.example.gossipwars.ui.actions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.proposals.ArmyOption
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.lang.ClassCastException
import java.lang.IllegalStateException

class NegotiateDialogFragment: DialogFragment() {
    internal lateinit var listener: NegotiateDialogListener
    var usernameSelected: String? = null
    var armyOption: ArmyOption? = null
    var increaseNr: Int? = null

    interface NegotiateDialogListener {
        fun onDialogPositiveClick(dialog: NegotiateDialogResult?)
        fun onDialogNegativeClick(dialog: NegotiateDialogResult?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NegotiateDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement NegotiateDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("activity can not be null")
        }
        var inflater = requireActivity().layoutInflater
        var builder = AlertDialog.Builder(activity)
        val negotiateView: View = inflater.inflate(R.layout.negotiate_form, null)
        val playersGroup: RadioGroup = negotiateView.findViewById(R.id.playersToRequestGroup)
        val optionsGroup: RadioGroup = negotiateView.findViewById(R.id.armyOptionsGroup)

        Game.players.value?.filter { player -> player.id != Game.myId }?.forEach { player: Player ->
            var playerButton = RadioButton(context)
            playerButton.text = player.username
            playersGroup.addView(playerButton)
        }
        if (playersGroup.childCount == 0) {
            for (i in 0..7) {
                val dummyButton = RadioButton(context)
                dummyButton.text = "player" + i.toString()
                playersGroup.addView(dummyButton)
            }
        }

        playersGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                val checkedRadioButton = playersGroup.findViewById<RadioButton>(checkedId)
                usernameSelected = checkedRadioButton.text.toString()
            }
        }

        optionsGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                val checkedButton = optionsGroup.findViewById<RadioButton>(checkedId)
                when(checkedButton.text.toString()) {
                    "Army size" -> armyOption = ArmyOption.SIZE
                    "Attack" -> armyOption = ArmyOption.ATTACK
                    "Defense" -> armyOption = ArmyOption.DEFEND
                }
            }
        }

        builder.setView(negotiateView)
        builder.setTitle("Select player to request, option and quantity")
            .setPositiveButton("Done") { _, _ ->
                val armyInput: TextInputEditText? =  negotiateView.findViewById(R.id.armyInputText)
                if (armyInput?.text.toString().isNotEmpty()) {
                    increaseNr = armyInput?.text.toString().toInt()
                } else {
                    increaseNr == -1
                }
                listener.onDialogPositiveClick(NegotiateDialogResult(usernameSelected, armyOption, increaseNr))
            }
            .setNegativeButton("Cancel") { _, _ ->
                listener.onDialogNegativeClick(null)
            }

        return builder.create()
    }


}