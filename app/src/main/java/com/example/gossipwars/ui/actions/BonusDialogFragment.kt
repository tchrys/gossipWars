package com.example.gossipwars.ui.actions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.gossipwars.logic.proposals.ArmyOption
import java.lang.IllegalStateException

class BonusDialogFragment: DialogFragment() {
    internal lateinit var listener: BonusDialogListener
    var stringToArmyOption = mutableMapOf("Army size" to ArmyOption.SIZE,
                                        "Attack" to ArmyOption.ATTACK, "Defense" to ArmyOption.DEFEND)
    var optionSelected: ArmyOption? = null

    interface BonusDialogListener {
        fun onDialogPositiveClick(dialog: ArmyOption?)
        fun onDialogNegativeClick(dialog: ArmyOption?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as BonusDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement BonusDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("activity can not be null")
        }
        val builder = AlertDialog.Builder(activity)

        var armyOptions = mutableListOf("Army size", "Attack", "Defense").toTypedArray()
        optionSelected = stringToArmyOption[armyOptions[0]]
        builder.setTitle("Select your bonus(size = 5k, attack / defense = +5)")
            .setSingleChoiceItems(armyOptions, 0) { _, which ->
                optionSelected = stringToArmyOption[armyOptions[which]]
            }
            .setPositiveButton("Done") { _, _ ->
                listener.onDialogPositiveClick(optionSelected)
            }
            .setNegativeButton("Cancel") { _, _ ->
                listener.onDialogNegativeClick(null)
            }
        return builder.create()
    }

}