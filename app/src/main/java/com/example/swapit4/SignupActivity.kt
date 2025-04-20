package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit4.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference

        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val name = binding.nameEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && phone.isNotEmpty()) {
                registerUser(email, password, name, phone)
            } else {
                if (email.isEmpty()) binding.emailEditText.error = "Email required"
                if (password.isEmpty()) binding.passwordEditText.error = "Password required"
                if (name.isEmpty()) binding.nameEditText.error = "Name required"
                if (phone.isEmpty()) binding.phoneEditText.error = "Phone required"
            }
        }

        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String, name: String, phone: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.signupButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                binding.signupButton.isEnabled = true

                if (task.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: ""
                                val user = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "phone" to phone
                                )

                                database.child("users").child(userId).setValue(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Registration successful! Please verify your email.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to send verification email",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}