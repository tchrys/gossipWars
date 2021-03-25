package com.example.gossipwars.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
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
import java.util.*
import kotlin.collections.ArrayList


class ActionsFragment : Fragment() {

    private lateinit var actionsViewModel: ActionsViewModel
    private var joinProps: List<Proposal>? = null
    private var kickProps: List<Proposal>? = null
    private var attackProps: List<Proposal>? = null
    private var defendProps: List<Proposal>? = null
    private var negotiateProps: List<ArmyRequest>? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        actionsViewModel = ViewModelProviders.of(this).get(ActionsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_actions, container, false)
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

        joinCardReply= root.findViewById(R.id.joinCardReply)
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

    fun sendVote(proposal: Proposal, boolean: Boolean) {

    }

    private fun kickChipSetup() {
        kickChip.setOnClickListener {
            if (Notifications.alliancesNoForMe.value!! == 0) {
                showSnackbarForError("You must belong to an alliance to do this action")
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
                    VoteProposalsDialog("Join proposals", joinProps as ArrayList<Proposal>
                    ).show(it, "joinVotesDialog")
                }
            } else {
                // just for debug
                val props: ArrayList<Proposal> = arrayListOf()
                for (i in 0..3) {
                    val alliance = Alliance(UUID.randomUUID())
                    alliance.name = "all" + i.toString()
                    props.add(
                        JoinProposal(
                            alliance, Player("mirel", UUID.randomUUID()),
                            Player("marcel", UUID.randomUUID()), proposalId = UUID.randomUUID()
                        )
                    )
                }
                fragmentManager?.let {
                    VoteProposalsDialog("Join proposals", props).show(
                        it,
                        "joinVotesDialog"
                    )
                }
            }
        }
    }

    private fun kickReplySetup() {
        kickCardReply.setOnClickListener {
            if (!kickProps.isNullOrEmpty()) {
                fragmentManager?.let {
                    VoteProposalsDialog("Kick proposals", kickProps as ArrayList<Proposal>
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
                        attackProps as ArrayList<Proposal>
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
                        defendProps as ArrayList<Proposal>
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
                    VoteNegotiateDialog("Negotiate requests",
                        negotiateProps as ArrayList<ArmyRequest>)
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
                        GameHelper.findMyProposals() as ArrayList<Proposal>
                    ).show(it, "yourReqDialog")
                }
            } else {
                showSnackbarForError("You have made no proposals yet")
            }
        }
    }

    private fun subscribeToJoinProps() {
        Notifications.joinPropsNo.observe(viewLifecycleOwner, Observer {
            joinCardText.text = getString(R.string.join_proposals, it)
            joinProps = GameHelper.findAllPropsFromCategory(ProposalEnum.JOIN)
        })
    }

    private fun subscribeToKickProps() {
        Notifications.kickPropsNo.observe(viewLifecycleOwner, Observer {
            kickCardText.text = getString(R.string.kick_proposals, it)
            kickProps = GameHelper.findAllPropsFromCategory(ProposalEnum.KICK)
        })
    }

    private fun subscribeToAttackProps() {
        Notifications.attackPropsNo.observe(viewLifecycleOwner, Observer {
            attackCardText.text = getString(R.string.attack_proposals, it)
            attackProps = GameHelper.findAllPropsFromCategory(ProposalEnum.ATTACK)
        })
    }

    private fun subscribeToDefenseProps() {
        Notifications.defensePropsNo.observe(viewLifecycleOwner, Observer {
            defendCardText.text = getString(R.string.defense_proposals, it)
            defendProps = GameHelper.findAllPropsFromCategory(ProposalEnum.DEFEND)
        })
    }

    private fun subscribeToNegotiateProps() {
        Notifications.negotiatePropsNo.observe(viewLifecycleOwner, Observer {
            negotiateCardText.text = getString(R.string.negotiate_proposals, it)
            negotiateProps = GameHelper.findMyArmyRequests()
        })
    }

    private fun subscribeToMyProps() {
        Notifications.myPropsNo.observe(viewLifecycleOwner, Observer {
            yourRequestsText.text = getString(R.string.your_proposals, it)
        })
    }

    private fun subscribeToChipStateEvents() {
        Notifications.myBonusTaken.observe(viewLifecycleOwner, Observer {
            if (it) {
                context?.let { it1 -> ContextCompat.getColor(it1, R.color.disabledChip) }
                    ?.let { it2 ->
                        bonusChip.setTextColor(
                            it2
                        )
                    }
            }
        })

        Notifications.alliancesNoForMe.observe(viewLifecycleOwner, Observer { value ->
            if (value == 0) {
                context?.let { ContextCompat.getColor(it, R.color.disabledChip) }?.let {
                    kickChip.setTextColor(it)
                    joinChip.setTextColor(it)
                    attackChip.setTextColor(it)
                    defendChip.setTextColor(it)
                }
            }
        })
    }

    private fun showSnackbarForError(message: String) {
        (activity as InGameActivity).showSnackBarOnError(R.id.fragment_actions_layout, message)
    }
}