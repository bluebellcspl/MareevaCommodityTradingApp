package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ReceiveChatItemBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ReceiveImageItemBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ReceiveVoiceItemBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.SentChatItemBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.SentImageItemBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.SentVoiceItemBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatResponseModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.ChatRecyclerViewHelper
import com.bumptech.glide.Glide

class ChatBoxMessageAdapter(
    var context: Context,
    var SenderId: String,
    var recyclerViewHelper: ChatRecyclerViewHelper
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var ITEM_SENT = 1
    var ITEM_RECEIVE = 2
    var IMAGE_ITEM_SENT = 10
    var IMAGE_ITEM_RECEIVE = 20
    var AUDIO_ITEM_SENT = 100
    var AUDIO_ITEM_RECEIVE = 200
    lateinit var handler: Handler
    private val chatList = mutableListOf<ChatResponseModel>()
    var isInitialDataLoaded = false
    val TAG = "ChatBoxMessageAdapter"

    //Message ViewHolder
    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = SentChatItemBinding.bind(view)
    }

    inner class ReceiveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = ReceiveChatItemBinding.bind(view)
    }

    //Image ViewHolder
    inner class SentImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = SentImageItemBinding.bind(view)
    }

    inner class ReceiveImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = ReceiveImageItemBinding.bind(view)
    }

    //Audio ViewHolder
    inner class SentAudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = SentVoiceItemBinding.bind(view)
    }

    inner class ReceiveAudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = ReceiveVoiceItemBinding.bind(view)
    }

    fun loadPreviousChat(previousChatList: ArrayList<ChatResponseModel>) {
        chatList.addAll(0, previousChatList)
        notifyItemRangeInserted(0, previousChatList.size)
    }

    fun loadInitialChat(previousChatList: ArrayList<ChatResponseModel>) {
        if (!chatList.containsAll(previousChatList))
        {
            chatList.addAll(0, previousChatList)
        }
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatResponseModel) {
        chatList.add(message)
//        notifyDataSetChanged()
        notifyItemInserted(chatList.size)
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

            IMAGE_ITEM_SENT -> view = SentImageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.sent_image_item, parent, false)
            )

            IMAGE_ITEM_RECEIVE -> view = ReceiveImageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.receive_image_item, parent, false)
            )

            AUDIO_ITEM_SENT -> view = SentAudioViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.sent_voice_item, parent, false)
            )

            AUDIO_ITEM_RECEIVE -> view = ReceiveAudioViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.receive_voice_item, parent, false)
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
        }
        else if (holder.itemViewType == ITEM_RECEIVE) {
            var viewHolder = holder as ReceiveViewHolder
            viewHolder.binding.tvMessageReceiveItem.setText(chatMessage.message)
            viewHolder.binding.tvDateReceiveItem.setText(chatMessage.Date)
        }
        else if (holder.itemViewType == IMAGE_ITEM_SENT) {
            var viewHolder = holder as SentImageViewHolder
            Glide.with(context)
                .load(chatMessage.FileMedia)
                .into(viewHolder.binding.ImgViewSentItem)
            viewHolder.binding.tvDateSentItem.setText(chatMessage.Date)
            viewHolder.binding.ImgViewSentItem.setOnClickListener {
                recyclerViewHelper.onImageItemClick(chatMessage)
            }
        }
        else if (holder.itemViewType == IMAGE_ITEM_RECEIVE) {
            var viewHolder = holder as ReceiveImageViewHolder
            Glide.with(context)
                .load(chatMessage.FileMedia)
                .into(viewHolder.binding.ImgViewReceiveItem)
            viewHolder.binding.tvDateReceiveItem.setText(chatMessage.Date)
            viewHolder.binding.ImgViewReceiveItem.setOnClickListener {
                recyclerViewHelper.onImageItemClick(chatMessage)
            }
        }
        else if (holder.itemViewType == AUDIO_ITEM_SENT) {
            var viewHolder = holder as SentAudioViewHolder
            val mediaPlayer = MediaPlayer()
            viewHolder.binding.tvDateSentItem.setText(chatMessage.Date)
            handler = Handler(Looper.getMainLooper())
            try {

                mediaPlayer.setDataSource(chatMessage.FileMedia)
                mediaPlayer.prepare()
                viewHolder.binding.voicePlayerSeekBarPlayerSentItem.max = mediaPlayer.duration

                viewHolder.binding.voicePlayerSeekBarPlayerSentItem.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                mediaPlayer.seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            // No implementation needed
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            // No implementation needed
                        }
                    })

                viewHolder.binding.btnPlayVoicePlayerSentItem.setOnClickListener {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                        handler.postDelayed(object : Runnable {
                            override fun run() {
                                if (mediaPlayer.isPlaying) {
                                    viewHolder.binding.voicePlayerSeekBarPlayerSentItem.progress =
                                        mediaPlayer.currentPosition
                                    handler.postDelayed(this, 200) // Update every second
                                }
                            }
                        }, 0)
                        viewHolder.binding.btnPauseVoicePlayerSentItem.visibility = View.VISIBLE
                        viewHolder.binding.btnPlayVoicePlayerSentItem.visibility = View.GONE
                    }
                }

                viewHolder.binding.btnPauseVoicePlayerSentItem.setOnClickListener {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        handler.removeCallbacksAndMessages(null)
                        viewHolder.binding.btnPauseVoicePlayerSentItem.visibility = View.GONE
                        viewHolder.binding.btnPlayVoicePlayerSentItem.visibility = View.VISIBLE
                    }
                }
                mediaPlayer.setOnCompletionListener {
                    viewHolder.binding.btnPauseVoicePlayerSentItem.visibility = View.GONE
                    viewHolder.binding.btnPlayVoicePlayerSentItem.visibility = View.VISIBLE
                    viewHolder.binding.voicePlayerSeekBarPlayerSentItem.progress = 0

                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "onBindViewHolder: ${e.message}")
            }
        }
        else if (holder.itemViewType == AUDIO_ITEM_RECEIVE) {
            var viewHolder = holder as ReceiveAudioViewHolder
            val mediaPlayer = MediaPlayer()
            viewHolder.binding.tvDateReceiveItem.setText(chatMessage.Date)
            handler = Handler(Looper.getMainLooper())
            try {

                mediaPlayer.setDataSource(chatMessage.FileMedia)
                mediaPlayer.prepare()
                viewHolder.binding.voicePlayerSeekBarPlayerReceiveItem.max = mediaPlayer.duration

                viewHolder.binding.voicePlayerSeekBarPlayerReceiveItem.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                mediaPlayer.seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            // No implementation needed
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            // No implementation needed
                        }
                    })
                viewHolder.binding.btnPlayVoicePlayerReceiveItem.setOnClickListener {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()

                        // Update seek bar progress continuously
                        handler.postDelayed(object : Runnable {
                            override fun run() {
                                if (mediaPlayer.isPlaying) {
                                    viewHolder.binding.voicePlayerSeekBarPlayerReceiveItem.progress =
                                        mediaPlayer.currentPosition
                                    handler.postDelayed(this, 200) // Update every second
                                }
                            }
                        }, 0)
                        viewHolder.binding.btnPauseVoicePlayerReceiveItem.visibility = View.VISIBLE
                        viewHolder.binding.btnPlayVoicePlayerReceiveItem.visibility = View.GONE
                    }
                }

                viewHolder.binding.btnPauseVoicePlayerReceiveItem.setOnClickListener {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        handler.removeCallbacksAndMessages(null)
                        viewHolder.binding.btnPauseVoicePlayerReceiveItem.visibility = View.GONE
                        viewHolder.binding.btnPlayVoicePlayerReceiveItem.visibility = View.VISIBLE
                    }
                }

                mediaPlayer.setOnCompletionListener {
                    viewHolder.binding.btnPauseVoicePlayerReceiveItem.visibility = View.GONE
                    viewHolder.binding.btnPlayVoicePlayerReceiveItem.visibility = View.VISIBLE
                    viewHolder.binding.voicePlayerSeekBarPlayerReceiveItem.progress = 0
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "onBindViewHolder: ${e.message}")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messageModel = chatList[position]
            ?: return -1 // Return an invalid view type if the messageModel object is null
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