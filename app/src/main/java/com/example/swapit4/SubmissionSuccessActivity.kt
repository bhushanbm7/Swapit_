package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit4.databinding.ActivitySubmissionSuccessBinding

class SubmissionSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmissionSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmissionSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewMarketplaceButton.setOnClickListener {
            navigateToMarketplace()
        }
    }

    private fun navigateToMarketplace() {
        val intent = Intent(this, MarketplaceActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}