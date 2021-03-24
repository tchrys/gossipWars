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
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.proposals.JoinProposal
import com.example.gossipwars.logic.proposals.KickProposal
import com.example.gossipwars.logic.proposals.Proposal
import com.example.gossipwars.logic.proposals.ProposalEnum
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

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ProposalListAdapter? = null
    private var username: String = "dsf"
    private var joinProps : List<Proposal>? = null
    private var kickProps : List<Proposal>? = null
    private var attackProps : List<Proposal>? = null
    private var defendProps : List<Proposal>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        actionsViewModel = ViewModelProviders.of(this).get(ActionsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_actions, container, false)
        val kickChip: Chip = root.findViewById(R.id.kickChip)
        val joinChip: Chip = root.findViewById(R.id.joinChip)
        val negotiateChip: Chip = root.findViewById(R.id.negotiateChip)
        val bonusChip: Chip = root.findViewById(R.id.roundBonus)
        val attackChip: Chip = root.findViewById(R.id.attackChip)
        val defendChip: Chip = root.findViewById(R.id.defendChip)

        val joinCardText: TextView = root.findViewById(R.id.joinCardText)
        val joinCardReply: Button = root.findViewById(R.id.joinCardReply)
        val kickCardText: TextView = root.findViewById(R.id.kickCardText)
        val kickCardReply: Button = root.findViewById(R.id.kickCardReply)
        val attackCardText: TextView = root.findViewById(R.id.attackCardText)
        val attackCardReply: Button = root.findViewById(R.id.attackCardReply)
        val defendCardText: TextView = root.findViewById(R.id.defendCardText)
        val defendCardReply: Button = root.findViewById(R.id.defendCardReply)
        val negotiateCardText: TextView = root.findViewById(R.id.negotiateCardText)
        val negotiateCardReply: Button = root.findViewById(R.id.negotiateCardReply)
        val yourRequestsText: TextView = root.findViewById(R.id.yourRequestsCardText)
        val yourRequestsReply: Button = root.findViewById(R.id.yourRequestsCardReply)


        kickChip.setOnClickListener {
            if (noAllianceForMe()) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else {
                fragmentManager?.let {
                    KickDialogFragment()
                        .show(it, "kickDialogTag")
                }
            }
        }


        joinChip.setOnClickListener {
            if (noAllianceForMe()) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else {
                fragmentManager?.let {
                    JoinDialogFragment()
                        .show(it, "joinDialogTag")
                }
            }
        }


        negotiateChip.setOnClickListener {
            fragmentManager?.let {
                NegotiateDialogFragment()
                    .show(it, "negotiateDialogTag")
            }
        }


        bonusChip.setOnClickListener {
            if (Game.myBonusTaken.value!!) {
                showSnackbarForError("You've already taken the bonus for this round")
            } else {
                fragmentManager?.let {
                    BonusDialogFragment()
                        .show(it, "bonusDialogTag")
                }
            }
        }


        attackChip.setOnClickListener {
            if (noAllianceForMe()) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else {
                fragmentManager?.let {
                    AttackDialogFragment()
                        .show(it, "attackDialogTag")
                }
            }
        }


        defendChip.setOnClickListener {
            if (noAllianceForMe()) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else {
                fragmentManager?.let {
                    DefendDialogFragment()
                        .show(it, "defendDialogTag")
                }
            }
        }

        Game.joinPropsNo.observe(viewLifecycleOwner, Observer {
            joinCardText.text = getString(R.string.join_proposals, it)
            joinProps = Game.findAllPropsFromCategory(ProposalEnum.JOIN)
        })
        joinCardReply.setOnClickListener {
            if (!joinProps.isNullOrEmpty()) {
                fragmentManager?.let { VoteProposalsDialog("Join proposals",
                    joinProps as ArrayList<Proposal>, username).show(it, "joinVotesDialog") }
            } else {
                // just for debug
                var props: ArrayList<Proposal> = arrayListOf()
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
                    VoteProposalsDialog("Join proposals", props, username).show(
                        it,
                        "joinVotesDialog"
                    )
                }
            }
        }

        Game.kickPropsNo.observe(viewLifecycleOwner, Observer {
            kickCardText.text = getString(R.string.kick_proposals, it)
            kickProps = Game.findAllPropsFromCategory(ProposalEnum.KICK)
        })
        kickCardReply.setOnClickListener {
            if (!kickProps.isNullOrEmpty()) {
                fragmentManager?.let { VoteProposalsDialog("Kick proposals",
                        kickProps as ArrayList<Proposal>, username).show(it, "kickVotesDialog") }
            } else {
                showSnackbarForError("There are no kick proposals yet")
            }
        }

        Game.attackPropsNo.observe(viewLifecycleOwner, Observer {
            attackCardText.text = getString(R.string.attack_proposals, it)
            attackProps = Game.findAllPropsFromCategory(ProposalEnum.ATTACK)
        })
        attackCardReply.setOnClickListener {
            if (!attackProps.isNullOrEmpty()) {
                fragmentManager?.let { VoteProposalsDialog("Attack proposals",
                    attackProps as ArrayList<Proposal>, username).show(it, "attackVotesDialog") }
            } else {
                showSnackbarForError("There are no attack proposals yet")
            }
        }

        Game.defensePropsNo.observe(viewLifecycleOwner, Observer {
            defendCardText.text = getString(R.string.defense_proposals, it)
            defendProps = Game.findAllPropsFromCategory(ProposalEnum.DEFEND)
        })
        defendCardReply.setOnClickListener {
            if (!defendProps.isNullOrEmpty()) {
                fragmentManager?.let { VoteProposalsDialog("Defense proposals",
                    defendProps as ArrayList<Proposal>, username).show(it, "defendVotesDialog") }
            } else {
                showSnackbarForError("There are no defense proposals yet")
            }
        }

        // TODO negotiate props

        Game.myPropsNo.observe(viewLifecycleOwner, Observer {
            yourRequestsText.text = getString(R.string.your_proposals, it)
        })
        yourRequestsReply.setOnClickListener {
            if (Game.myPropsNo.value!! > 0) {
                fragmentManager?.let { VoteProposalsDialog("Your requests",
                    Game.findMyProposals() as ArrayList<Proposal>, username).show(it, "yourReqDialog") }
            } else {
                showSnackbarForError("You have made no proposals yet")
            }
        }


        Game.myBonusTaken.observe(viewLifecycleOwner, Observer {
            if (it) {
                context?.let { it1 -> ContextCompat.getColor(it1, R.color.disabledChip) }
                    ?.let { it2 ->
                        bonusChip.setTextColor(
                            it2
                        )
                    }
            }
        })

        if (noAllianceForMe()) {
            context?.let { ContextCompat.getColor(it, R.color.disabledChip) }?.let {
                kickChip.setTextColor(it)
                joinChip.setTextColor(it)
                attackChip.setTextColor(it)
                defendChip.setTextColor(it)
            }
        }

        return root
    }

    fun sendVote(proposal: Proposal, boolean: Boolean) {

    }

    fun noAllianceForMe(): Boolean = Game.findAlliancesForPlayer(Game.myId)?.size == 0

    fun showSnackbarForError(message: String) {
        (activity as InGameActivity).showSnackBarOnError(R.id.fragment_actions_layout, message)
    }
}