package com.example.linkup.model

import com.google.firebase.Timestamp

data class UserModel(

    var phone: String = "",
    var username: String = "",
    var createdTimestamp: Timestamp = Timestamp.now(),
    var userId: String? = null

) {
    constructor() : this("", "", Timestamp.now(),"")
}
