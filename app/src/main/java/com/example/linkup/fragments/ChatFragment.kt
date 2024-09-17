package com.example.linkup.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkup.R
import com.example.linkup.adapter.RecentChatRecyclerAdapter
import com.example.linkup.databinding.FragmentChatBinding
import com.example.linkup.model.ChatroomModel
import com.example.linkup.util.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: RecentChatRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val query = FirebaseUtil.currentUserId()?.let {
            FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", it)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
        }

        val options = query?.let {
            FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(it, ChatroomModel::class.java)
                .build()
        }

        adapter = options?.let { RecentChatRecyclerAdapter(it, requireContext()) }!!
        binding.recylerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recylerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}
