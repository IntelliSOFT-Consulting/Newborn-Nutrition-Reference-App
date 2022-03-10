package com.imeja.demo.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.imeja.demo.FhirApplication
import com.imeja.demo.MainActivity
import com.imeja.demo.R
import com.imeja.demo.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySplashBinding
    private var isWelcomed: Boolean = false
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isWelcomed = FhirApplication.isWelcomed(this)
        isLoggedIn = FhirApplication.isLoggedIn(this)

        Handler().postDelayed({
            if (isWelcomed) {
                if (isLoggedIn) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
            } else {
                startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
            }
            finish()
        }, 3000)
    }
}