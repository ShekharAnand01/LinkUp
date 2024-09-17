package com.example.linkup.model

import com.google.firebase.Timestamp

data class ChatroomModel(
    var chatroomId: String? = null,
    var userIds: List<String?>? = null,
    var lastMessageTimestamp: Timestamp? = null,
    var lastMessageSenderId: String? = null,
    var lastMessage: String? = null
)
