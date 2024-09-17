package com.example.linkup.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.databinding.RecentChatRecyclerRowBinding
import com.example.linkup.fragments.ChatActivity
import com.example.linkup.model.ChatroomModel
import com.example.linkup.model.UserModel
import com.example.linkup.util.AndroidUtil
import com.example.linkup.util.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class RecentChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatroomModel>,
    private val context: Context
) : FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder>(options) {

    override fun onBindViewHolder(holder: ChatroomModelViewHolder, position: Int, model: ChatroomModel) {
        val currentUserId = FirebaseUtil.currentUserId()
        val lastMessageSentByMe = model.lastMessageSenderId == currentUserId

        FirebaseUtil.getOtherUserFromChatroom(model.userIds ?: emptyList())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val otherUserModel = task.result?.toObject(UserModel::class.java)
                    otherUserModel?.let { user ->
                        // Set Profile Picture
                        val profilePicRef = FirebaseUtil.getOtherProfilePicStorageRef(user.userId ?: "")
                        profilePicRef.downloadUrl
                            .addOnCompleteListener { picTask ->
                                if (picTask.isSuccessful) {
                                    val uri = picTask.result
                                    uri?.let {
                                        AndroidUtil.setProfilePic(holder.binding.profilePicView.profilePicImageView.context, uri, holder.binding.profilePicView.profilePicImageView)
                                    } ?: run {
                                        // Handle null URI case (e.g., set placeholder image)
                                    }
                                } else {
                                    // Handle error case
                                }
                            }

                        // Set User Details
                        holder.binding.userNameText.text = user.username
                        holder.binding.lastMessageText.text = if (lastMessageSentByMe) {
                            "You: ${model.lastMessage}"
                        } else {
                            model.lastMessage
                        }
                        holder.binding.lastMessageTimeText.text =
                            model.lastMessageTimestamp?.let { timestamp ->
                                FirebaseUtil.timestampToString(timestamp)
                            } ?: "Unknown time"

                        // Set Click Listener
                        holder.itemView.setOnClickListener {
                            val intent = Intent(holder.itemView.context, ChatActivity::class.java).apply {
                                AndroidUtil.passUserModelAsIntent(this, user)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            holder.itemView.context.startActivity(intent)
                        }
                    }
                } else {
                    // Handle failure in fetching user data
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val binding = RecentChatRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatroomModelViewHolder(binding)
    }

    inner class ChatroomModelViewHolder(val binding: RecentChatRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)
}
