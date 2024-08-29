package com.example.linkup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.linkup.databinding.ActivityLoginOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginOtpActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    private lateinit var binding: ActivityLoginOtpBinding
    private lateinit var phoneNumber: String
    private var verificationCode: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        phoneNumber = intent.extras?.getString("phone") ?: ""
        sendOtp(phoneNumber, false)

        binding.loginNextBtn.setOnClickListener {
            val enteredOtp = binding.loginOtp.text.toString()
            val credential = PhoneAuthProvider.getCredential(verificationCode ?: "", enteredOtp)
            signIn(credential)
        }

        binding.resendOtpTextview.setOnClickListener {
            sendOtp(phoneNumber, true)
        }
    }

    private fun sendOtp(phoneNumber: String, isResend: Boolean) {
        startResendTimer()
        setInProgress(true)

        val optionsBuilder = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    signIn(phoneAuthCredential)
                    setInProgress(false)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    showToast("OTP verification failed")
                    setInProgress(false)
                }

                override fun onCodeSent(
                    verificationId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationCode = verificationId
                    resendingToken = forceResendingToken
                    showToast("OTP sent successfully")
                    setInProgress(false)
                }
            })

        if (isResend) {
            resendingToken?.let { optionsBuilder.setForceResendingToken(it) }
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginNextBtn.visibility = View.GONE
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.loginNextBtn.visibility = View.VISIBLE
        }
    }

    private fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        setInProgress(true)
        mAuth.signInWithCredential(phoneAuthCredential)
            .addOnCompleteListener(this) { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    val intent = Intent(this, LoginUsernameActivity::class.java)
                    intent.putExtra("phone", phoneNumber)
                    startActivity(intent)
                } else {
                    showToast("OTP verification failed")
                }
            }
    }

    private fun startResendTimer() {
        binding.resendOtpTextview.isEnabled = false
        object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.resendOtpTextview.text = "Resend OTP in ${millisUntilFinished / 1000} seconds"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.resendOtpTextview.isEnabled = true
                binding.resendOtpTextview.text = "Resend OTP"
            }
        }.start()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

    }
}