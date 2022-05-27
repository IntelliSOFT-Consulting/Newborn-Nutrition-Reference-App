package com.intellisoft.nndak.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySplashBinding
    private var isWelcomed: Boolean = false
    private var isLoggedIn: Boolean = false
    private var isServerSet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isWelcomed = FhirApplication.isWelcomed(this)
        isLoggedIn = FhirApplication.isLoggedIn(this)
        isServerSet = FhirApplication.isServerSet(this)

        Handler().postDelayed({
            if (isWelcomed) {
            /*    if (isServerSet) {*/
                    if (isLoggedIn) {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    } else {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    }
             /*   } else {

                    startActivity(Intent(this@SplashActivity, SetupActivity::class.java))
                }*/
            } else {
                startActivity(Intent(this@SplashActivity, GetStartedActivity::class.java))
            }
            finish()
        }, 3000)
    }
}