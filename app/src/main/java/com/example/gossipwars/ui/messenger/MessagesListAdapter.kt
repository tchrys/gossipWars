package com.example.gossipwars.ui.messenger

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.ChatMessage
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.ui.messenger.MessagesListAdapter.MessageViewHolder
import java.util.*

class MessagesListAdapter(
    private val context: MessengerActivity, private val messagesList: ArrayList<ChatMessage>,
    private val myUsername: String
) : RecyclerView.Adapter<MessageViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    inner class MessageViewHolder(
        itemView: View,
        adapter: MessagesListAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val messageAuthor: TextView = itemView.findViewById(R.id.messageAuthor)
        val messageContent: TextView = itemView.findViewById(R.id.messageContent)
        val messageDate: TextView = itemView.findViewById(R.id.messageDate)
        val cardView: CardView = itemView.findViewById(R.id.messageCardView)
        val messageLinearLayout: LinearLayout = itemView.findViewById(R.id.messageLinearLayout)
        val mAdapter: MessagesListAdapter = adapter
        override fun onClick(view: View) {
            // get the position of the view that was clicked
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // inflate an item view
        Log.d("DBG", "on create view holder")
        val mItemView = mInflater.inflate(R.layout.messages_items, parent, false)
        return MessageViewHolder(mItemView, this)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chatMessage = messagesList[position]
        val senderId = chatMessage.sender.id
        holder.messageAuthor.text = chatMessage.sender.username
        holder.messageContent.text = chatMessage.content
        var msgDate =
            chatMessage.messageDate[Calendar.HOUR_OF_DAY].toString()
        msgDate += ":"
        val messageMinutes =
            chatMessage.messageDate[Calendar.MINUTE].toString()
        msgDate += if (messageMinutes.length == 1) "0$messageMinutes" else messageMinutes
        holder.messageDate.text = msgDate
        val displayWidth = context.resources.displayMetrics.widthPixels
        val layoutParams =
            holder.cardView.layoutParams as MarginLayoutParams
        holder.cardView.minimumWidth = displayWidth / 6

//        if (chatMessage.getMessageDate().get(Calendar.MILLISECOND) % 2 == 1) {
//            Log.d("DBG", chatMessage.getContent() + " e pe random");
//            senderId = UUID.randomUUID();
//        }
        if (senderId == Game.myId) {
            Log.d("DBG", chatMessage.content + " e verde")
            holder.cardView.setCardBackgroundColor(
                context.resources.getColor(R.color.light_green)
            )
            holder.messageLinearLayout.gravity = Gravity.END
            layoutParams.marginStart = displayWidth / 4
            layoutParams.marginEnd = 0
            holder.cardView.requestLayout()
            holder.messageAuthor.visibility = View.GONE
        } else {
            holder.cardView.setCardBackgroundColor(
                context.resources.getColor(R.color.light_blue)
            )
            holder.messageLinearLayout.gravity = Gravity.START
            layoutParams.marginStart = 0
            layoutParams.marginEnd = displayWidth / 4
            if (position > 0 && messagesList[position - 1].sender.id == senderId) {
                holder.messageAuthor.visibility = View.GONE
                layoutParams.marginStart = 50
            }
            holder.cardView.requestLayout()
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

}