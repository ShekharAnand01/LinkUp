package com.example.linkup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.linkup.databinding.ActivityLoginPhoneNumberBinding

class LoginPhoneNumberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPhoneNumberBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        binding.loginProgressBar.visibility = View.GONE

        binding.loginCountrycode.registerCarrierNumberEditText(binding.loginMobileNumber)
        binding.sendOtpBtn.setOnClickListener {
            if (!binding.loginCountrycode.isValidFullNumber) {
                binding.loginMobileNumber.error = "Phone number not valid"
                return@setOnClickListener
            }
            val intent = Intent(this, LoginOtpActivity::class.java).apply {
                putExtra("phone", binding.loginCountrycode.fullNumberWithPlus)
            }
            startActivity(intent)
        }
    }
}

