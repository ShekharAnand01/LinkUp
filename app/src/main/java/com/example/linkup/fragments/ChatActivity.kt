package com.example.linkup.fragments

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.adapter.ChatRecyclerAdapter
import com.example.linkup.databinding.ActivityChatBinding
import com.example.linkup.model.ChatMessageModel
import com.example.linkup.model.ChatroomModel
import com.example.linkup.model.UserModel
import com.example.linkup.util.AndroidUtil
import com.example.linkup.util.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var otherUser: UserModel
    private lateinit var chatroomId: String
    private lateinit var chatroomModel: ChatroomModel
    private lateinit var adapter: ChatRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get UserModel
        otherUser = AndroidUtil.getUserModelFromIntent(intent)
        chatroomId = FirebaseUtil.currentUserId()
            ?.let { otherUser.userId?.let { it1 -> FirebaseUtil.getChatroomId(it, it1) } }.toString()

        otherUser.userId?.let {
//            FirebaseUtil.getOtherProfilePicStorageRef(it).downloadUrl
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val uri = task.result
//                        AndroidUtil.setProfilePic(this, uri, binding.profilePicLayout)
//                    }
//                }
        }

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.otherUsername.text = otherUser.username

        binding.messageSendBtn.setOnClickListener {
            val message = binding.chatMessageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessageToUser(message)
            }
        }

        getOrCreateChatroomModel()
        setupChatRecyclerView()
    }

    private fun setupChatRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java)
            .build()

        adapter = ChatRecyclerAdapter(options, applicationContext)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
        }
        binding.chatRecyclerView.adapter = adapter
        adapter.startListening()
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.chatRecyclerView.smoothScrollToPosition(0)
            }
        })
    }

    private fun sendMessageToUser(message: String) {
        chatroomModel.apply {
            lastMessageTimestamp = Timestamp.now()
            lastMessageSenderId = FirebaseUtil.currentUserId()
            lastMessage = message
        }

        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel)

        val chatMessageModel = ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now())
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.chatMessageInput.text.clear()
//                    sendNotification(message)
                }
            }
    }

    private fun getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                chatroomModel = task.result?.toObject(ChatroomModel::class.java)
                    ?: ChatroomModel(
                        chatroomId,
                        listOf(FirebaseUtil.currentUserId(), otherUser.userId),
                        Timestamp.now(),
                        ""
                    )
                FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel)
            }
        }
    }

//    private fun sendNotification(message: String) {
//        FirebaseUtil.currentUserDetails().get().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val currentUser = task.result?.toObject(UserModel::class.java)
//                val jsonObject = JSONObject().apply {
//                    put("notification", JSONObject().apply {
//                        put("title", currentUser?.username)
//                        put("body", message)
//                    })
//                    put("data", JSONObject().apply {
//                        put("userId", currentUser?.userId)
//                    })
//                    put("to", otherUser.fcmToken)
//                }
//
//                callApi(jsonObject)
//            }
//        }
//    }
//
//    private fun callApi(jsonObject: JSONObject) {
//        val JSON = MediaType.get("application/json; charset=utf-8")
//        val client = OkHttpClient()
//        val url = "https://fcm.googleapis.com/fcm/send"
//        val body = RequestBody.create(JSON, jsonObject.toString())
//        val request = Request.Builder()
//            .url(url)
//            .post(body)
//            .header("Authorization", "Bearer YOUR_API_KEY")
//            .build()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Handle failure
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                // Handle response
//            }
//        })
//    }
}
