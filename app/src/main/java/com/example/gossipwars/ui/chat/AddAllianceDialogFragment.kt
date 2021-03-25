package com.example.gossipwars.ui.chat

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Game
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
        builder.setView(inputView)
        var playerOptionsString: Array<CharSequence>? = Game.players.value?.filter { player -> player.id != Game.myId }
            ?.map { player -> player.username }?.toTypedArray()
        if (playerOptionsString?.size == 0) {
            var mtl = playerOptionsString.toMutableList()
            mtl.add("ex1")
            mtl.add("ex2")
            mtl.add("ex3")
            mtl.add("ex4")
            mtl.add("ex5")
            mtl.add("ex6")
            mtl.add("ex7")
            playerOptionsString = mtl.toTypedArray()
        }
        usernameSelected = playerOptionsString?.get(0).toString()
        builder.setTitle("Add alliance name and first member")
            .setSingleChoiceItems(playerOptionsString, 0) { _, which ->
                usernameSelected = playerOptionsString?.get(which).toString()
            }
            .setPositiveButton("Done") { _, _ ->
                val allianceNameInput: TextInputEditText? =  inputView.findViewById(R.id.sharedInputText)
                listener.onDialogPositiveClick(AllianceAfterDialog(usernameSelected, allianceNameInput?.text.toString()))
            }
            .setNegativeButton("Cancel") { _, _ -> }
        return builder.create()
    }
}