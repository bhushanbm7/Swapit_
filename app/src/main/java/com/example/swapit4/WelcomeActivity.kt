package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit4.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.farmerButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            intent.putExtra("USER_TYPE", "Farmer")
            startActivity(intent)
        }

        binding.consumerButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            intent.putExtra("USER_TYPE", "Consumer")
            startActivity(intent)
        }
    }
}