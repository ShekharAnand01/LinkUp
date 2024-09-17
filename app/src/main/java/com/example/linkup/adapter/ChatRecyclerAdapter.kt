package com.example.linkup.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.databinding.ChatMessageRecyclerRowBinding
import com.example.linkup.model.ChatMessageModel
import com.example.linkup.util.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatMessageModel>,
    private val context: Context
) : FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder>(options) {

    override fun onBindViewHolder(holder: ChatModelViewHolder, position: Int, model: ChatMessageModel) {
        if (model.senderId == FirebaseUtil.currentUserId()) {
            holder.binding.leftChatLayout.visibility = View.GONE
            holder.binding.rightChatLayout.visibility = View.VISIBLE
            holder.binding.rightChatTextview.text = model.message
        } else {
            holder.binding.rightChatLayout.visibility = View.GONE
            holder.binding.leftChatLayout.visibility = View.VISIBLE
            holder.binding.leftChatTextview.text = model.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatModelViewHolder {
        val binding = ChatMessageRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatModelViewHolder(binding)
    }

    inner class ChatModelViewHolder(val binding: ChatMessageRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)
}
