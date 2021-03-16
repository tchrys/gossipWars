package com.example.gossipwars

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gossipwars.communication.messages.allianceCommunication.ArmyRequestDTO
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.proposals.ArmyOption
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.example.gossipwars.ui.actions.*
import com.example.gossipwars.ui.chat.AddAllianceDialogFragment
import com.example.gossipwars.ui.chat.AllianceAfterDialog
import com.google.android.material.snackbar.Snackbar

class InGameActivity : AppCompatActivity(),
                        AddAllianceDialogFragment.AllianceDialogListener,
                        KickDialogFragment.KickDialogListener,
                        JoinDialogFragment.JoinDialogListener,
                        NegotiateDialogFragment.NegotiateDialogListener,
                        BonusDialogFragment.BonusDialogListener,
                        AttackDialogFragment.AttackDialogListener,
                        DefendDialogFragment.DefendDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map, R.id.navigation_chat, R.id.navigation_overview,
                R.id.navigation_actions
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // parent to get main activity

        Game.sendMyInfo()

        Toast.makeText(this, Game.roomInfo?.roomName, Toast.LENGTH_LONG).show()

//        while (!Game.gameStarted) {
//
//        }
//
//        Toast.makeText(this, Game.players.map { player -> player.username }
//            .joinToString(","), Toast.LENGTH_LONG).show()

    }

    override fun onDialogPositiveClick(dialog: AllianceAfterDialog?) {
        dialog?.firstMemberUsername?.let { Log.d("DBG", it) }
        if (dialog?.allianceName == null || dialog.firstMemberUsername == null
                                        || dialog.allianceName.isEmpty()) {
            showSnackBarOnError(R.id.fragment_chat_layout, "Please complete all fields")
        } else {
            val alliance: Alliance = Game.addAlliance(Game.findPlayerByUUID(Game.myId), dialog.allianceName)
            Game.findPlayerByUsername(dialog.firstMemberUsername)?.id?.let {
                Game.sendAllianceDTO(alliance.convertToDTO(),
                    it
                )
            }
            Log.d("DBG", Game.alliances.size.toString())
        }
    }

    override fun onDialogNegativeClick(dialog: AllianceAfterDialog?) {
        // do nothing
    }

    override fun onDialogPositiveClick(dialog: KickDialogResult?) {
        dialog?.allianceName?.let { Log.d("DBG", it) }
        dialog?.usernameSelected?.let { Log.d("DBG", it) }
        if (dialog?.allianceName == null || dialog?.usernameSelected == null) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            val meAsAPlayer = Game.findPlayerByUUID(Game.myId)
            val alliance: Alliance? = Game.findAllianceByName(dialog.allianceName)
            val player: Player? = Game.findPlayerByUsername(dialog.usernameSelected)
            if (alliance != null && player != null) {
                meAsAPlayer.makeProposal(alliance, player, ProposalEnum.KICK, 0)
            }
        }
    }

    override fun onDialogNegativeClick(dialog: KickDialogResult?) {
        // do nothing
    }

    override fun onDialogPositiveClick(dialog: JoinDialogResult?) {
        dialog?.allianceName?.let { Log.d("DBG", it) }
        dialog?.usernameSelected?.let { Log.d("DBG", it) }
        if (dialog?.allianceName == null || dialog?.usernameSelected == null) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            val meAsAPlayer = Game.findPlayerByUUID(Game.myId)
            val alliance: Alliance? = Game.findAllianceByName(dialog.allianceName)
            val player: Player? = Game.findPlayerByUsername(dialog.usernameSelected)
            if (alliance != null && player != null) {
                meAsAPlayer.makeProposal(alliance, player, ProposalEnum.JOIN, 0)
            }
        }
    }

    override fun onDialogNegativeClick(dialog: JoinDialogResult?) {
        // do nothing
    }

    override fun onDialogPositiveClick(dialog: NegotiateDialogResult?) {
        dialog?.usernameSelected?.let { Log.d("DBG", it) }
        dialog?.armyOption?.let { Log.d("DBG", it.toString()) }
        dialog?.increase?.let { Log.d("DBG", it.toString()) }
        if (dialog?.usernameSelected == null || dialog.armyOption == null ||
                        dialog.increase == null || dialog.increase == -1) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            val playerSelected: Player? = dialog.usernameSelected.let { Game.findPlayerByUsername(it) }
            if (playerSelected != null) {
                when(dialog.armyOption) {
                    ArmyOption.SIZE -> Game.sendArmyRequest(ArmyRequestDTO(Game.myId,
                                            playerSelected.id, dialog.armyOption, dialog.increase))
                    else -> Game.sendArmyRequest(ArmyRequestDTO(Game.myId, playerSelected.id,
                                                    dialog.armyOption, dialog.increase))
                }
            }
        }

    }

    override fun onDialogNegativeClick(dialog: NegotiateDialogResult?) {
        // do nothing
    }

    override fun onDialogPositiveClick(dialog: ArmyOption?) {
        dialog?.let { Log.d("DBG", it.toString()) }
        when(dialog) {
            ArmyOption.ATTACK -> Game.sendArmyAction(ArmyRequestDTO(Game.myId, Game.myId,
                                                                    ArmyOption.ATTACK, 5))
            ArmyOption.DEFEND -> Game.sendArmyAction(ArmyRequestDTO(Game.myId, Game.myId,
                                                                    ArmyOption.DEFEND, 5))
            ArmyOption.SIZE -> Game.sendArmyAction(ArmyRequestDTO(Game.myId, Game.myId,
                                                                    ArmyOption.SIZE, 5000))
        }
        Game.myBonusTaken.value = true
    }

    override fun onDialogNegativeClick(dialog: ArmyOption?) {
        // do nothing
    }

    fun showSnackBarOnError(viewId: Int, message: String) {
        val snackbar = Snackbar.make(findViewById(viewId), message, Snackbar.LENGTH_SHORT)
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.textError))
        snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        snackbar.show()
    }

    override fun onDialogPositiveClick(dialog: AttackDialogResult?) {
        dialog?.allianceName?.let { Log.d("DBG", it) }
        dialog?.regionName?.let { Log.d("DBG", it) }
        // TODO
    }

    override fun onDialogNegativeClick(dialog: AttackDialogResult?) {
        // do nothing
    }

    override fun onDialogPositiveClick(dialog: DefendDialogResult?) {
        dialog?.allianceName?.let { Log.d("DBG", it) }
        dialog?.regionName?.let { Log.d("DBG", it) }
        // TODO
    }

    override fun onDialogNegativeClick(dialog: DefendDialogResult?) {
        // do nothing
    }

}