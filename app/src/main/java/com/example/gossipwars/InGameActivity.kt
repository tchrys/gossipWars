package com.example.gossipwars

import android.graphics.ColorFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gossipwars.communication.messages.actions.TroopsActionDTO
import com.example.gossipwars.communication.messages.allianceCommunication.ArmyRequestDTO
import com.example.gossipwars.communication.messages.allianceCommunication.ProposalResponse
import com.example.gossipwars.logic.actions.MembersAction
import com.example.gossipwars.logic.entities.*
import com.example.gossipwars.logic.proposals.ArmyOption
import com.example.gossipwars.logic.proposals.ArmyRequest
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.example.gossipwars.ui.actions.VoteNegotiateDialog
import com.example.gossipwars.ui.actions.VoteNegotiateResult
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

class InGameActivity : AppCompatActivity(),
    AddAllianceDialogFragment.AllianceDialogListener,
    KickDialogFragment.KickDialogListener,
    JoinDialogFragment.JoinDialogListener,
    NegotiateDialogFragment.NegotiateDialogListener,
    BonusDialogFragment.BonusDialogListener,
    AttackDialogFragment.AttackDialogListener,
    DefendDialogFragment.DefendDialogListener,
    RegionDialogFragment.RegionDialogListener,
    VoteProposalsDialog.VoteDialogListener,
    VoteNegotiateDialog.NegotiateDialogListener {

    private lateinit var frameLayout: FrameLayout
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        frameLayout = findViewById(R.id.progress_view)
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


        Game.inGameActivity = this

        Game.sendMyInfo()

        Notifications.allianceNewStructure.observe(this, androidx.lifecycle.Observer {
            if (!navView.menu.getItem(1).isChecked && Game.alliances.size > 0) {
                navView.menu.getItem(1).setIcon(R.drawable.ic_chat_notification)
                Notifications.messageEmitter.values.forEach { mutableLiveData: MutableLiveData<ChatMessage> ->
                    mutableLiveData.observe(this, androidx.lifecycle.Observer {
                        navView.menu.getItem(1).setIcon(R.drawable.ic_chat_notification)
                    })
                }
            }
        })

        listOf(Notifications.joinPropsNo, Notifications.kickPropsNo, Notifications.attackPropsNo,
            Notifications.defensePropsNo, Notifications.negotiatePropsNo)
            .forEach { mutableLiveData: MutableLiveData<Int> ->
                mutableLiveData.observe(this, androidx.lifecycle.Observer {
                    if (it > 0 && !navView.menu.getItem(3).isChecked) {
                        navView.menu.getItem(3).setIcon(R.drawable.ic_actions_notification)
                    }
                })
            }

        Notifications.roundOngoing.observe(this, androidx.lifecycle.Observer {
            frameLayout.visibility = if (it) View.GONE else View.VISIBLE
        })


        Notifications.roundStoppedFor.observe(this, androidx.lifecycle.Observer {
            if (it > 10) {
                Game.gameEndCleanup()
                finish()
            }
        })

    }

    fun chatSeen() {
        navView.menu.getItem(1).setIcon(R.drawable.ic_chat)
    }

    fun proposalsSeen() {
        navView.menu.getItem(3).setIcon(R.drawable.ic_actions)
    }

    override fun onBackPressed() {
        Game.sendSurrender()
        finish()
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.surrenderButton) {
            Game.sendSurrender()
            finish()
            return true
        } else {
            onBackPressed()
        }
        return true
    }

    override fun onDialogPositiveClick(dialog: AllianceAfterDialog) {
        if (dialog.allianceName.isNullOrEmpty() || dialog.firstMemberUsername == null) {
            showSnackBarOnError(R.id.fragment_chat_layout, "Please complete all fields")
        } else {
            val alliance: Alliance =
                Game.addAlliance(GameHelper.findPlayerByUUID(Game.myId), dialog.allianceName)
            GameHelper.findPlayerByUsername(dialog.firstMemberUsername)?.id?.let {
                GlobalScope.launch {
                    Game.sendAllianceDTO(
                        alliance.convertToDTO(),
                        it
                    )
                }
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

    private fun onMemberProposalSent(
        allianceName: String,
        usernameSelected: String,
        propEnum: ProposalEnum
    ) {
        val meAsAPlayer = GameHelper.findPlayerByUUID(Game.myId)
        val alliance: Alliance? = GameHelper.findAllianceByName(allianceName)
        val player: Player? = GameHelper.findPlayerByUsername(usernameSelected)
        if (alliance != null && player != null) {
            meAsAPlayer.makeProposal(alliance, player, propEnum, 0)
        }
    }

    override fun onDialogPositiveClick(dialog: NegotiateDialogResult) {
        if (dialog.usernameSelected == null || dialog.armyOption == null ||
            dialog.increase == null || dialog.increase == -1
        ) {
            showSnackBarOnError(R.id.fragment_actions_layout, "Please complete all fields")
        } else {
            val playerSelected: Player? =
                dialog.usernameSelected.let { GameHelper.findPlayerByUsername(it) }
            if (playerSelected != null) {
                when (dialog.armyOption) {
                    ArmyOption.SIZE -> GlobalScope.launch {
                        Game.sendArmyRequest(
                            ArmyRequestDTO(
                                Game.myId,
                                playerSelected.id,
                                dialog.armyOption,
                                dialog.increase,
                                UUID.randomUUID()
                            )
                        )
                    }
                    else -> GlobalScope.launch {
                        Game.sendArmyRequest(
                            ArmyRequestDTO(
                                Game.myId, playerSelected.id,
                                dialog.armyOption, dialog.increase, UUID.randomUUID()
                            )
                        )
                    }
                }
            }
        }

    }

    override fun onDialogPositiveClick(dialog: ArmyOption?) {
        val armyRequestDTO: ArmyRequestDTO? = when (dialog) {
            ArmyOption.ATTACK -> ArmyRequestDTO(
                Game.myId,
                Game.myId,
                ArmyOption.ATTACK,
                5,
                UUID.randomUUID()
            )
            ArmyOption.DEFEND -> ArmyRequestDTO(
                Game.myId,
                Game.myId,
                ArmyOption.DEFEND,
                5,
                UUID.randomUUID()
            )
            ArmyOption.SIZE -> ArmyRequestDTO(
                Game.myId,
                Game.myId,
                ArmyOption.SIZE,
                5000,
                UUID.randomUUID()
            )
            else -> null
        }
        armyRequestDTO?.let { GlobalScope.launch { Game.sendArmyAction(it) } }
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

    private fun onStrategyProposalSent(
        allianceName: String,
        regionName: String,
        propEnum: ProposalEnum
    ) {
        val meAsAPlayer = GameHelper.findPlayerByUUID(Game.myId)
        val alliance: Alliance? = GameHelper.findAllianceByName(allianceName)
        val region: Region? = GameHelper.findRegionByName(regionName)
        val target: Player? = region?.occupiedBy
        if (alliance != null && target != null) {
            meAsAPlayer.makeProposal(alliance, target, propEnum, region.id)
        }
    }

    override fun onDialogPositiveClick(dialog: RegionDialogResult) {
        val regionFrom: Region? = dialog.regionFrom?.let { GameHelper.findRegionByName(it) }
        val regionTo: Region? = dialog.regionTo?.let { GameHelper.findRegionByName(it) }
        if (regionFrom != null && regionTo != null) {
            val meAsAPlayer = GameHelper.findPlayerByUUID(Game.myId)
            meAsAPlayer.soldiersUsedThisRound[regionFrom.id] =
                meAsAPlayer.soldiersUsedThisRound.getOrElse(regionFrom.id) { 0 } + dialog.size
            GlobalScope.launch {
                Game.sendTroopsAction(
                    TroopsActionDTO(
                        Game.myId,
                        regionFrom.id,
                        regionTo.id,
                        dialog.size
                    )
                )
            }
        }
    }

    override fun onDialogPositiveClick(dialog: VoteProposalsResult) {
        dialog.responseList.forEach {
            GlobalScope.launch {
                Game.sendProposalResponse(
                    ProposalResponse(it.allianceId, it.proposalId, it.response, Game.myId)
                )
            }
            val alliance = GameHelper.findAllianceByUUID(it.allianceId)
            val propEnum: ProposalEnum? = alliance.proposalsList.find { proposal ->
                proposal.proposalId == it.proposalId
            }?.proposalEnum
            val idx =
                alliance.proposalsList.indexOfFirst { proposal ->
                    proposal.proposalId == it.proposalId && proposal.initiator.id != Game.myId
                }
            if (idx != -1) {
                propEnum.let { proposalEnum ->
                    if (proposalEnum != null) {
                        updateProposalNotifications(proposalEnum)
                    }
                }
                alliance.proposalsList.removeAt(idx)
            }
        }
    }

    private fun updateProposalNotifications(propEnum: ProposalEnum) {
        when (propEnum) {
            ProposalEnum.KICK -> Notifications.kickPropsNo.value =
                Notifications.kickPropsNo.value?.minus(1)
            ProposalEnum.JOIN -> Notifications.joinPropsNo.value =
                Notifications.joinPropsNo.value?.minus(1)
            ProposalEnum.DEFEND -> Notifications.defensePropsNo.value =
                Notifications.defensePropsNo.value?.minus(1)
            ProposalEnum.ATTACK -> Notifications.attackPropsNo.value =
                Notifications.attackPropsNo.value?.minus(1)
        }
    }

    override fun onDialogPositiveClick(dialog: VoteNegotiateResult) {
        val myArmyRequest: MutableList<ArmyRequest> = GameHelper.findMyArmyRequests()
        dialog.yesList.forEach { yesRequest: ArmyRequest ->
            GlobalScope.launch { Game.sendArmyApproval(yesRequest.convertToDTO()) }
        }
        dialog.yesList.plus(dialog.noList).forEach { request: ArmyRequest ->
            val idx = myArmyRequest.indexOfFirst { armyRequest -> armyRequest.id == request.id }
            if (idx != -1) {
                Notifications.negotiatePropsNo.value =
                    Notifications.negotiatePropsNo.value?.minus(1)
                myArmyRequest.removeAt(idx)
            }
        }
    }

    fun showSnackBarOnError(viewId: Int, message: String) {
        val snackbar = Snackbar.make(findViewById(viewId), message, Snackbar.LENGTH_SHORT)
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.textError))
        snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        snackbar.show()
    }

}