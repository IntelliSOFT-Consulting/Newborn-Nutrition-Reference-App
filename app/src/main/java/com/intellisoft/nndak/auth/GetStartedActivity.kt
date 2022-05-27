package com.intellisoft.nndak.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.ActivityGetStartedBinding
import com.intellisoft.nndak.databinding.ActivityLoginBinding

class GetStartedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGetStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        binding.apply {
            btnSubmit.setOnClickListener {
                FhirApplication.setWelcomed(this@GetStartedActivity, true)
                val i = Intent(this@GetStartedActivity, LoginActivity::class.java)
                startActivity(i)
                finish()
            }
        }

    }
}