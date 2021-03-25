package com.example.gossipwars

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gossipwars.communication.messages.allianceCommunication.ArmyRequestDTO
import com.example.gossipwars.logic.entities.*
import com.example.gossipwars.logic.proposals.ArmyOption
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.example.gossipwars.ui.actions.VoteProposalsDialog
import com.example.gossipwars.ui.actions.VoteProposalsResult
import com.example.gossipwars.ui.chat.AddAllianceDialogFragment
import com.example.gossipwars.ui.chat.AllianceAfterDialog
import com.example.gossipwars.ui.dialogs.attack.AttackDialogFragment
import com.example.gossipwars.ui.dialogs.attack.AttackDialogResult
import com.example.gossipwars.ui.dialogs.bonus.BonusDialogFragment
import com.example.gossipwars.ui.dialogs.defend.DefendDialogFragment
import com.example.gossipwars.ui.dialogs.defend.DefendDialogResult
import com.example.gossipwars.ui.dialogs.join.JoinDialogFragment
import com.example.gossipwars.ui.dialogs.join.JoinDialogResult
import com.example.gossipwars.ui.dialogs.kick.KickDialogFragment
import com.example.gossipwars.ui.dialogs.kick.KickDialogResult
import com.example.gossipwars.ui.dialogs.negotiate.NegotiateDialogFragment
import com.example.gossipwars.ui.dialogs.negotiate.NegotiateDialogResult
import com.example.gossipwars.ui.dialogs.region.RegionDialogFragment
import com.example.gossipwars.ui.dialogs.region.RegionDialogResult
import com.google.android.material.snackbar.Snackbar

class InGameActivity : AppCompatActivity(),
                        AddAllianceDialogFragment.AllianceDialogListener,
                        KickDialogFragment.KickDialogListener,
                        JoinDialogFragment.JoinDialogListener,
                        NegotiateDialogFragment.NegotiateDialogListener,
                        BonusDialogFragment.BonusDialogListener,
                        AttackDialogFragment.AttackDialogListener,
                        DefendDialogFragment.DefendDialogListener,
                        RegionDialogFragment.RegionDialogListener,
                        VoteProposalsDialog.VoteDialogListener {

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
        Game.sendMyInfo()

    }

    override fun onDialogPositiveClick(dialog: AllianceAfterDialog) {
        if (dialog.allianceName.isNullOrEmpty() || dialog.firstMemberUsername == null) {
            showSnackBarOnError(R.id.fragment_chat_layout, "Please complete all fields")
        } else {
            val alliance: Alliance = Game.addAlliance(GameHelper.findPlayerByUUID(Game.myId), dialog.allianceName)
            GameHelper.findPlayerByUsername(dialog.firstMemberUsername)?.id?.let {
                Game.sendAllianceDTO(alliance.convertToDTO(),
                    it
                )
            }
        }
    }

    override fun onDialogPositiveClick(dialog: KickDialogResult) {
        if (dialog.allianceName == null || dialog.usernameSelected == null) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            onMemberProposalSent(dialog.allianceName, dialog.usernameSelected, ProposalEnum.KICK)
        }
    }

    override fun onDialogPositiveClick(dialog: JoinDialogResult) {
        if (dialog.allianceName == null || dialog.usernameSelected == null) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            onMemberProposalSent(dialog.allianceName, dialog.usernameSelected, ProposalEnum.JOIN)
        }
    }

    private fun onMemberProposalSent(allianceName: String, usernameSelected: String, propEnum: ProposalEnum) {
        val meAsAPlayer = GameHelper.findPlayerByUUID(Game.myId)
        val alliance: Alliance? = GameHelper.findAllianceByName(allianceName)
        val player: Player? = GameHelper.findPlayerByUsername(usernameSelected)
        if (alliance != null && player != null) {
            meAsAPlayer.makeProposal(alliance, player, propEnum, 0)
        }
    }

    override fun onDialogPositiveClick(dialog: NegotiateDialogResult) {
        if (dialog.usernameSelected == null || dialog.armyOption == null ||
                        dialog.increase == null || dialog.increase == -1) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            val playerSelected: Player? = dialog.usernameSelected.let { GameHelper.findPlayerByUsername(it) }
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

    override fun onDialogPositiveClick(dialog: ArmyOption?) {
        when(dialog) {
            ArmyOption.ATTACK -> Game.sendArmyAction(ArmyRequestDTO(Game.myId, Game.myId,
                                                                    ArmyOption.ATTACK, 5))
            ArmyOption.DEFEND -> Game.sendArmyAction(ArmyRequestDTO(Game.myId, Game.myId,
                                                                    ArmyOption.DEFEND, 5))
            ArmyOption.SIZE -> Game.sendArmyAction(ArmyRequestDTO(Game.myId, Game.myId,
                                                                    ArmyOption.SIZE, 5000))
        }
        Notifications.myBonusTaken.value = true
    }

    override fun onDialogPositiveClick(dialog: AttackDialogResult) {
        if (dialog.allianceName == null || dialog.regionName == null) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            onStrategyProposalSent(dialog.allianceName, dialog.regionName, ProposalEnum.ATTACK)
        }
    }

    override fun onDialogPositiveClick(dialog: DefendDialogResult) {
        if (dialog.allianceName == null || dialog.regionName == null) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            onStrategyProposalSent(dialog.allianceName, dialog.regionName, ProposalEnum.DEFEND)
        }
    }

    private fun onStrategyProposalSent(allianceName: String, regionName: String, propEnum: ProposalEnum) {
        val meAsAPlayer = GameHelper.findPlayerByUUID(Game.myId)
        val alliance: Alliance? = GameHelper.findAllianceByName(allianceName)
        val region: Region? = GameHelper.findRegionByName(regionName)
        val target: Player? = region?.occupiedBy
        if (alliance != null && target != null) {
            meAsAPlayer.makeProposal(alliance, target, propEnum, region.id)
        }
    }

    override fun onDialogPositiveClick(dialog: RegionDialogResult) {
        // TODO
    }

    override fun onDialogPositiveClick(dialog: VoteProposalsResult) {
        // TODO
    }

    fun showSnackBarOnError(viewId: Int, message: String) {
        val snackbar = Snackbar.make(findViewById(viewId), message, Snackbar.LENGTH_SHORT)
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.textError))
        snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        snackbar.show()
    }

}