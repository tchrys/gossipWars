package com.example.gossipwars.ui.chat

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import com.google.android.material.textfield.TextInputEditText

class AddAllianceDialogFragment : DialogFragment() {
    internal lateinit var listener: AllianceDialogListener
    var usernameSelected: String? = null

    interface AllianceDialogListener {
        fun onDialogPositiveClick(dialog: AllianceAfterDialog)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as AllianceDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("activity can not be null")
        }
        val inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(activity)
        val inputView: View = inflater.inflate(R.layout.shared_input_name, null)

        val firstMemberGroup: RadioGroup = inputView.findViewById(R.id.addFirstMemberRadioGroup)
        Game.players.value?.filter { player -> player.id != Game.myId }?.forEach { player: Player ->
            val playerButton = RadioButton(context)
            playerButton.text = player.username
            firstMemberGroup.addView(playerButton)
        }
        if (firstMemberGroup.childCount == 0) {
            for (i in 0..7) {
                val dummyButton = RadioButton(context)
                dummyButton.text = "player" + i.toString()
                firstMemberGroup.addView(dummyButton)
            }
        }

        firstMemberGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                val checkedRadioButton = firstMemberGroup.findViewById<RadioButton>(checkedId)
                usernameSelected = checkedRadioButton.text.toString()
            }
        }

        builder.setView(inputView)
        builder.setTitle("Add alliance name and first member")
            .setPositiveButton("Done") { _, _ ->
                val allianceNameInput: TextInputEditText? =  inputView.findViewById(R.id.sharedInputText)
                listener.onDialogPositiveClick(AllianceAfterDialog(usernameSelected, allianceNameInput?.text.toString()))
            }
            .setNegativeButton("Cancel") { _, _ -> }
        return builder.create()
    }
}