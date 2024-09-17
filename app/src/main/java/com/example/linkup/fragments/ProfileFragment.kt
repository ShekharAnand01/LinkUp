package com.example.linkup.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.linkup.R
import com.example.linkup.SplashActivity
import com.example.linkup.databinding.FragmentProfileBinding
import com.example.linkup.model.UserModel
import com.example.linkup.util.AndroidUtil
import com.example.linkup.util.FirebaseUtil
import com.google.firebase.messaging.FirebaseMessaging


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentUserModel: UserModel
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize image picker launcher to handle the result from the gallery
        imagePickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    AndroidUtil.setProfilePic(requireContext(), uri, binding.profileImageView)
                }
            }
        }

        // Initialize permission launcher for handling permission requests
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery() // Permission granted, open gallery
            } else {
                AndroidUtil.showToast(requireContext(), "Storage permission denied")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        getUserData()

        binding.profleUpdateBtn.setOnClickListener {
            updateBtnClick()
        }

        binding.logoutBtn.setOnClickListener {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseUtil.logout()
                    val intent = Intent(requireContext(), SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }

        // Set OnClickListener for selecting profile picture
        binding.profileImageView.setOnClickListener {
            checkStoragePermission() // Check storage permission before opening gallery
        }

        return view
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Request the permission using ActivityResultLauncher
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            // Permission already granted, open gallery
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickLauncher.launch(intent)
    }

    private fun updateBtnClick() {
        val newUsername = binding.profileUsername.text.toString()
        if (newUsername.isEmpty() || newUsername.length < 3) {
            binding.profileUsername.error = "Username length should be at least 3 chars"
            return
        }
        currentUserModel.username = newUsername
        setInProgress(true)

        selectedImageUri?.let {
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(it).addOnCompleteListener { task ->
                updateToFirestore()
            }
        } ?: updateToFirestore()
    }

    private fun updateToFirestore() {
        FirebaseUtil.currentUserDetails().set(currentUserModel).addOnCompleteListener { task ->
            setInProgress(false)
            val message = if (task.isSuccessful) "Updated successfully" else "Update failed"
            AndroidUtil.showToast(requireContext(), message)
        }
    }

    private fun getUserData() {
        setInProgress(true)

        FirebaseUtil.getCurrentProfilePicStorageRef().downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                AndroidUtil.setProfilePic(requireContext(), task.result, binding.profileImageView)
            }
        }

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener { task ->
            setInProgress(false)
            currentUserModel = task.result.toObject(UserModel::class.java)!!
            binding.profileUsername.setText(currentUserModel.username)
            binding.profilePhone.setText(currentUserModel.phone)
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.profileProgressBar.visibility = View.VISIBLE
            binding.profleUpdateBtn.visibility = View.GONE
        } else {
            binding.profileProgressBar.visibility = View.GONE
            binding.profleUpdateBtn.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}