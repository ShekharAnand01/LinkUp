package com.example.linkup.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class FirebaseUtil {
    companion object {

        private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
        private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
        private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

        fun currentUserId(): String? = auth.uid

        fun isLoggedIn(): Boolean = currentUserId() != null

        fun currentUserDetails(): DocumentReference =
            firestore.collection("users").document(currentUserId() ?: "")

        fun allUserCollectionReference(): CollectionReference =
            firestore.collection("users")

        fun getChatroomReference(chatroomId: String): DocumentReference =
            firestore.collection("chatrooms").document(chatroomId)

        fun getChatroomMessageReference(chatroomId: String): CollectionReference =
            getChatroomReference(chatroomId).collection("chats")

        fun getChatroomId(userId1: String, userId2: String): String {
            return if (userId1.hashCode() < userId2.hashCode()) {
                "$userId1+_+$userId2"
            } else {
                "$userId2+_+$userId1"
            }
        }

        fun allChatroomCollectionReference(): CollectionReference =
            firestore.collection("chatrooms")

        fun getOtherUserFromChatroom(userIds: List<String?>): DocumentReference {
            return if (userIds[0] == currentUserId()) {
                allUserCollectionReference().document(userIds[1]?:"")
            } else {
                allUserCollectionReference().document(userIds[0]?:"")
            }
        }

        fun timestampToString(timestamp: Timestamp): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
        }

        fun logout() {
            auth.signOut()
        }

        fun getCurrentProfilePicStorageRef(): StorageReference {
            return storage.reference.child("profile_pic")
                .child(currentUserId() ?: "")
        }

        fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
            return storage.reference.child("profile_pic")
                .child(otherUserId)
        }
    }
}
