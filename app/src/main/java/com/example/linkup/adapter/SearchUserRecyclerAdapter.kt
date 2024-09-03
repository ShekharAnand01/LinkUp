package com.example.linkup.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.databinding.SearchUserRecyclerRowBinding
import com.example.linkup.fragments.ChatActivity
import com.example.linkup.model.UserModel
import com.example.linkup.util.AndroidUtil
import com.example.linkup.util.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class SearchUserRecyclerAdapter(
    options: FirestoreRecyclerOptions<UserModel>,
    private val context: Context
) : FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder>(options) {

    override fun onBindViewHolder(holder: UserModelViewHolder, position: Int, model: UserModel) {
        holder.bind(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserModelViewHolder {
        val binding = SearchUserRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserModelViewHolder(binding)
    }

    inner class UserModelViewHolder(private val binding: SearchUserRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: UserModel) {
            binding.userNameText.text= model.username
            binding.phoneText.text = model.phone
            if (model.userId == FirebaseUtil.currentUserId()) {
                binding.userNameText.text = "${model.username} (Me)"
            }

//            FirebaseUtil.getOtherProfilePicStorageRef(model.userId).getDownloadUrl()
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val uri = task.result
//                        uri?.let { AndroidUtil.setProfilePic(context, it, binding.profilePic) }
//                    }
//                }

            binding.root.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    AndroidUtil.passUserModelAsIntent(this, model)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }
}