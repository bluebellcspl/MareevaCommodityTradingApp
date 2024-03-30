package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ReceiveChatItemBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.SentChatItemBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatResponseModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class ChatBoxMessageAdapter(
    var context: Context,
    var SenderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var ITEM_SENT = 1
    var ITEM_RECEIVE = 2
    var IMAGE_ITEM_SENT = 10
    var IMAGE_ITEM_RECEIVE = 20
    var AUDIO_ITEM_SENT = 100
    var AUDIO_ITEM_RECEIVE = 200
    lateinit var handler: Handler
    private val chatList = mutableListOf<ChatResponseModel>()
    val TAG = "ChatBoxMessageAdapter"

    //Message ViewHolder
    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = SentChatItemBinding.bind(view)
    }

    inner class ReceiveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = ReceiveChatItemBinding.bind(view)
    }

    fun loadPreviousChat(previousChatList: ArrayList<ChatResponseModel>) {
        chatList.addAll(previousChatList)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatResponseModel) {
        chatList.add(message)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: RecyclerView.ViewHolder? = null
        when (viewType) {
            ITEM_SENT -> view = SentViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.sent_chat_item, parent, false)
            )

            ITEM_RECEIVE -> view = ReceiveViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.receive_chat_item, parent, false)
            )

        }

        return view!!
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var chatMessage = chatList[holder.adapterPosition]
        if (holder.itemViewType == ITEM_SENT) {
            var viewHolder = holder as SentViewHolder
            viewHolder.binding.tvMessageSentItem.setText(chatMessage.message)
            viewHolder.binding.tvDateSentItem.setText(chatMessage.Date)
        } else if (holder.itemViewType == ITEM_RECEIVE) {
            var viewHolder = holder as ReceiveViewHolder
            viewHolder.binding.tvMessageReceiveItem.setText(chatMessage.message)
            viewHolder.binding.tvDateReceiveItem.setText(chatMessage.Date)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messageModel = chatList[position]
        return if (messageModel.FromUser == SenderId) {
            when (messageModel.MessageType) {
                "text" -> ITEM_SENT
                "file" -> IMAGE_ITEM_SENT
                "audio" -> AUDIO_ITEM_SENT
                else -> ITEM_SENT
            }

        } else {
            when (messageModel.MessageType) {
                "text" -> ITEM_RECEIVE
                "file" -> IMAGE_ITEM_RECEIVE
                "audio" -> AUDIO_ITEM_RECEIVE
                else -> ITEM_RECEIVE
            }

        }
    }
}