package com.example.linkup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.linkup.databinding.ActivityLoginUsernameBinding
import com.example.linkup.model.UserModel
import com.example.linkup.util.FirebaseUtil
import com.google.firebase.Timestamp

class LoginUsernameActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var binding: ActivityLoginUsernameBinding
    private lateinit var phoneNumber: String
    private var userModel: UserModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_username)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityLoginUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneNumber = intent.extras?.getString("phone") ?: ""
        getUsername()

        binding.loginLetMeInBtn.setOnClickListener {
            setUsername()
        }
    }

    private fun setUsername() {
        val username = binding.loginUsername.text.toString()
        if (username.isEmpty() || username.length < 3) {
            binding.loginUsername.error = "Username length should be at least 3 chars"
            return
        }
        setInProgress(true)

        if (userModel != null) {
            userModel?.username = username
        } else {
            userModel = UserModel(phoneNumber, username, Timestamp.now())
        }

        FirebaseUtil.currentUserDetails().set(userModel!!)
            .addOnCompleteListener { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    val intent = Intent(this@LoginUsernameActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
    }

    private fun getUsername() {
        setInProgress(true)
        FirebaseUtil.currentUserDetails().get()
            .addOnCompleteListener { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    userModel = task.result?.toObject(UserModel::class.java)
                    userModel?.let {
                        binding.loginUsername.setText(it.username)
                    }
                }
            }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginLetMeInBtn.visibility = View.GONE
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.loginLetMeInBtn.visibility = View.VISIBLE
        }
    }
    }
