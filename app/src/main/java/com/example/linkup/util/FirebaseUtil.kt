package com.example.linkup.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FirebaseUtil {
    companion object {

        private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
        private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
        private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
        fun currentUserId(): String? = auth.uid

        fun isLoggedIn(): Boolean = currentUserId() != null

        fun currentUserDetails(): DocumentReference =
            firestore.collection("users").document(currentUserId() ?: "")
    }
}
