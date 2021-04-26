package com.example.gossipwars.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.*
import com.example.gossipwars.logic.proposals.*
import com.example.gossipwars.ui.dialogs.attack.AttackDialogFragment
import com.example.gossipwars.ui.dialogs.bonus.BonusDialogFragment
import com.example.gossipwars.ui.dialogs.defend.DefendDialogFragment
import com.example.gossipwars.ui.dialogs.join.JoinDialogFragment
import com.example.gossipwars.ui.dialogs.kick.KickDialogFragment
import com.example.gossipwars.ui.dialogs.negotiate.NegotiateDialogFragment
import com.google.android.material.chip.Chip


class ActionsFragment : Fragment() {

    private lateinit var actionsViewModel: ActionsViewModel
    private var joinProps: List<ProposalVoteContent>? = null
    private var kickProps: List<ProposalVoteContent>? = null
    private var attackProps: List<ProposalVoteContent>? = null
    private var defendProps: List<ProposalVoteContent>? = null
    private var negotiateProps: List<NegotiateVoteContent>? = null

    private lateinit var kickChip: Chip
    private lateinit var joinChip: Chip
    private lateinit var negotiateChip: Chip
    private lateinit var bonusChip: Chip
    private lateinit var attackChip: Chip
    private lateinit var defendChip: Chip

    private lateinit var joinCardText: TextView
    private lateinit var kickCardText: TextView
    private lateinit var attackCardText: TextView
    private lateinit var defendCardText: TextView
    private lateinit var negotiateCardText: TextView
    private lateinit var yourRequestsText: TextView

    private lateinit var joinCardReply: Button
    private lateinit var kickCardReply: Button
    private lateinit var attackCardReply: Button
    private lateinit var defendCardReply: Button
    private lateinit var negotiateCardReply: Button
    private lateinit var yourRequestsReply: Button

    private lateinit var fragmentBarTitle: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        actionsViewModel = ViewModelProviders.of(this).get(ActionsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_actions, container, false)
        fragmentBarTitle = (context as InGameActivity).supportActionBar?.title.toString()

        subscribeToTimer()

        kickChip = root.findViewById(R.id.kickChip)
        joinChip = root.findViewById(R.id.joinChip)
        negotiateChip = root.findViewById(R.id.negotiateChip)
        bonusChip = root.findViewById(R.id.roundBonus)
        attackChip = root.findViewById(R.id.attackChip)
        defendChip = root.findViewById(R.id.defendChip)

        joinCardText = root.findViewById(R.id.joinCardText)
        kickCardText = root.findViewById(R.id.kickCardText)
        attackCardText = root.findViewById(R.id.attackCardText)
        defendCardText = root.findViewById(R.id.defendCardText)
        negotiateCardText = root.findViewById(R.id.negotiateCardText)
        yourRequestsText = root.findViewById(R.id.yourRequestsCardText)

        joinCardReply = root.findViewById(R.id.joinCardReply)
        kickCardReply = root.findViewById(R.id.kickCardReply)
        attackCardReply = root.findViewById(R.id.attackCardReply)
        defendCardReply = root.findViewById(R.id.defendCardReply)
        negotiateCardReply = root.findViewById(R.id.negotiateCardReply)
        yourRequestsReply = root.findViewById(R.id.yourRequestsCardReply)

        kickChipSetup()
        joinChipSetup()
        attackChipSetup()
        defendChipSetup()
        bonusChipSetup()
        negotiateChipSetup()

        joinReplySetup()
        kickReplySetup()
        attackReplySetup()
        defendReplySetup()
        negotiateReplySetup()
        yourRequestsSetup()

        subscribeToJoinProps()
        subscribeToKickProps()
        subscribeToAttackProps()
        subscribeToDefenseProps()
        subscribeToMyProps()
        subscribeToNegotiateProps()
        subscribeToChipStateEvents()

        return root
    }

    private fun kickChipSetup() {
        kickChip.setOnClickListener {
            if (Notifications.alliancesNoForMe.value!! == 0) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else if (!GameHelper.existsAlliancesWith3(Game.myId)) {
                showSnackbarForError("There is no alliance with at least 3 players")
            } else {
                fragmentManager?.let {
                    KickDialogFragment()
                        .show(it, "kickDialogTag")
                }
            }
        }
    }

    private fun joinChipSetup() {
        joinChip.setOnClickListener {
            if (Notifications.alliancesNoForMe.value!! == 0) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else if (Game.players.value?.size!! <= 2 || !GameHelper.existsAllianceWithJoinOption(
                    Game.myId
                )
            ) {
                showSnackbarForError("Not enough players")
            } else {
                fragmentManager?.let {
                    JoinDialogFragment()
                        .show(it, "joinDialogTag")
                }
            }
        }
    }

    private fun attackChipSetup() {
        attackChip.setOnClickListener {
            if (Notifications.alliancesNoForMe.value!! == 0) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else if (GameHelper.findAttackableRegions().isEmpty()) {
                showSnackbarForError("You must have soldiers in enemy regions to do this")
            } else {
                fragmentManager?.let {
                    AttackDialogFragment()
                        .show(it, "attackDialogTag")
                }
            }
        }
    }

    private fun defendChipSetup() {
        defendChip.setOnClickListener {
            if (Notifications.alliancesNoForMe.value!! == 0) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else if (GameHelper.findDefendableRegions().isEmpty()) {
                showSnackbarForError("No region match this action")
            } else {
                fragmentManager?.let {
                    DefendDialogFragment()
                        .show(it, "defendDialogTag")
                }
            }
        }
    }

    private fun negotiateChipSetup() {
        negotiateChip.setOnClickListener {
            fragmentManager?.let {
                NegotiateDialogFragment()
                    .show(it, "negotiateDialogTag")
            }
        }
    }

    private fun bonusChipSetup() {
        bonusChip.setOnClickListener {
            if (Notifications.myBonusTaken.value!!) {
                showSnackbarForError("You've already taken the bonus for this round")
            } else if (GameHelper.findPlayerByUUID(Game.myId).capitalRegion == -1) {
                showSnackbarForError("You've lost all regions.")
            } else {
                fragmentManager?.let {
                    BonusDialogFragment()
                        .show(it, "bonusDialogTag")
                }
            }
        }
    }

    private fun joinReplySetup() {
        joinCardReply.setOnClickListener {
            if (!joinProps.isNullOrEmpty()) {
                fragmentManager?.let {
                    VoteProposalsDialog(
                        "Join proposals", joinProps as ArrayList<ProposalVoteContent>
                    ).show(it, "joinVotesDialog")
                }
            } else {
                showSnackbarForError("There are no join proposals yet")
            }
        }
    }

    private fun kickReplySetup() {
        kickCardReply.setOnClickListener {
            if (!kickProps.isNullOrEmpty()) {
                fragmentManager?.let {
                    VoteProposalsDialog(
                        "Kick proposals", kickProps as ArrayList<ProposalVoteContent>
                    ).show(it, "kickVotesDialog")
                }
            } else {
                showSnackbarForError("There are no kick proposals yet")
            }
        }
    }

    private fun attackReplySetup() {
        attackCardReply.setOnClickListener {
            if (!attackProps.isNullOrEmpty()) {
                fragmentManager?.let {
                    VoteProposalsDialog(
                        "Attack proposals",
                        attackProps as ArrayList<ProposalVoteContent>
                    ).show(it, "attackVotesDialog")
                }
            } else {
                showSnackbarForError("There are no attack proposals yet")
            }
        }
    }

    private fun defendReplySetup() {
        defendCardReply.setOnClickListener {
            if (!defendProps.isNullOrEmpty()) {
                fragmentManager?.let {
                    VoteProposalsDialog(
                        "Defense proposals",
                        defendProps as ArrayList<ProposalVoteContent>
                    ).show(it, "defendVotesDialog")
                }
            } else {
                showSnackbarForError("There are no defense proposals yet")
            }
        }
    }

    private fun negotiateReplySetup() {
        negotiateCardReply.setOnClickListener {
            if (Notifications.negotiatePropsNo.value!! > 0) {
                fragmentManager?.let {
                    VoteNegotiateDialog(
                        "Negotiate requests",
                        negotiateProps as ArrayList<NegotiateVoteContent>
                    )
                        .show(it, "negotiateReqDialog")
                }
            } else {
                showSnackbarForError("There are no negotiate requests yet")
            }
        }
    }

    private fun yourRequestsSetup() {
        yourRequestsReply.setOnClickListener {
            if (Notifications.myPropsNo.value!! > 0) {
                fragmentManager?.let {
                    VoteProposalsDialog(
                        "Your requests",
                        GameHelper.findMyProposals()?.map { proposal ->
                            when (proposal.proposalEnum) {
                                ProposalEnum.KICK -> Snapshots.generateContentFromKickProposal(
                                    proposal as KickProposal
                                )
                                ProposalEnum.JOIN -> Snapshots.generateContentFromJoinProposal(
                                    proposal as JoinProposal
                                )
                                ProposalEnum.ATTACK -> Snapshots.generateContentFromAttackProposal(
                                    proposal as StrategyProposal
                                )
                                ProposalEnum.DEFEND -> Snapshots.generateContentFromDefendProposal(
                                    proposal as StrategyProposal
                                )
                            }
                        } as ArrayList<ProposalVoteContent>
                    ).show(it, "yourReqDialog")
                }
            } else {
                showSnackbarForError("You have made no proposals yet")
            }
        }
    }

    private fun subscribeToJoinProps() {
        Notifications.joinPropsNo.observe(viewLifecycleOwner, Observer {
            if (Game.gameStarted) {
                joinCardText.text = getString(R.string.join_proposals, it)
                joinProps =
                    GameHelper.findAllPropsFromCategory(ProposalEnum.JOIN)?.map { proposal ->
                        Snapshots.generateContentFromJoinProposal(proposal as JoinProposal)
                    }
            }
        })
    }

    private fun subscribeToKickProps() {
        Notifications.kickPropsNo.observe(viewLifecycleOwner, Observer {
            if (Game.gameStarted) {
                kickCardText.text = getString(R.string.kick_proposals, it)
                kickProps =
                    GameHelper.findAllPropsFromCategory(ProposalEnum.KICK)?.map { proposal ->
                        Snapshots.generateContentFromKickProposal(proposal as KickProposal)
                    }
            }
        })
    }

    private fun subscribeToAttackProps() {
        Notifications.attackPropsNo.observe(viewLifecycleOwner, Observer {
            if (Game.gameStarted) {
                attackCardText.text = getString(R.string.attack_proposals, it)
                attackProps =
                    GameHelper.findAllPropsFromCategory(ProposalEnum.ATTACK)?.map { proposal ->
                        Snapshots.generateContentFromAttackProposal(proposal as StrategyProposal)
                    }
            }
        })
    }

    private fun subscribeToDefenseProps() {
        Notifications.defensePropsNo.observe(viewLifecycleOwner, Observer {
            if (Game.gameStarted) {
                defendCardText.text = getString(R.string.defense_proposals, it)
                defendProps =
                    GameHelper.findAllPropsFromCategory(ProposalEnum.DEFEND)?.map { proposal ->
                        Snapshots.generateContentFromDefendProposal(proposal as StrategyProposal)
                    }
            }
        })
    }

    private fun subscribeToNegotiateProps() {
        Notifications.negotiatePropsNo.observe(viewLifecycleOwner, Observer {
            if (Game.gameStarted) {
                negotiateCardText.text = getString(R.string.negotiate_proposals, it)
                negotiateProps = GameHelper.findMyArmyRequests().map { armyRequest ->
                    Snapshots.generateContentFromNegotiateProposal(armyRequest)
                }
            }
        })
    }

    private fun subscribeToMyProps() {
        Notifications.myPropsNo.observe(viewLifecycleOwner, Observer {
            if (Game.gameStarted) {
                yourRequestsText.text = getString(R.string.your_proposals, it)
            }
        })
    }

    private fun subscribeToChipStateEvents() {
        Notifications.myBonusTaken.observe(viewLifecycleOwner, Observer {
            if (Game.gameStarted) {
                val noCapital: Boolean = GameHelper.findPlayerByUUID(Game.myId).capitalRegion == -1
                val chipColor = if (it || noCapital) R.color.textError else R.color.light_green
                context?.let { it1 -> ContextCompat.getColor(it1, chipColor) }
                    ?.let { it2 ->
                        bonusChip.setTextColor(
                            it2
                        )
                    }
            }
        })

        Notifications.alliancesNoForMe.observe(viewLifecycleOwner, Observer { value ->
            if (Game.gameStarted) {
                val chipColor = if (value == 0) R.color.textError else R.color.light_green
                context?.let { ContextCompat.getColor(it, chipColor) }?.let {
                    kickChip.setTextColor(it)
                    joinChip.setTextColor(it)
                    attackChip.setTextColor(it)
                    defendChip.setTextColor(it)
                }
            }
        })
    }

    private fun subscribeToTimer() {
        Notifications.roundTimer.observe(viewLifecycleOwner, Observer {
            if (Game.gameStarted) {
                if (it > 5) {
                    (context as InGameActivity).supportActionBar?.title =
                        getString(
                            R.string.bar_title,
                            fragmentBarTitle,
                            GameHelper.roundTimeToString(it)
                        )
                } else {
                    (context as InGameActivity).supportActionBar?.title =
                        HtmlCompat.fromHtml(
                            getString(
                                R.string.bar_title_alert, fragmentBarTitle,
                                GameHelper.roundTimeToString(it)
                            ), HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                }
            }
        })
    }

    private fun showSnackbarForError(message: String) {
        (activity as InGameActivity).showSnackBarOnError(R.id.fragment_actions_layout, message)
    }
}