package com.intellisoft.nndak.auth

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.data.LoginData
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.databinding.ActivityPinLockBinding

class PinLockActivity : AppCompatActivity() {

    private val apiService = RestManager()
    private lateinit var binding: ActivityPinLockBinding
    private lateinit var mPinLockView: PinLockView
    private lateinit var mIndicatorDots: IndicatorDots
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            mPinLockView = pinLockView
            mIndicatorDots = indicatorDots

            mPinLockView.attachIndicatorDots(mIndicatorDots)
            mPinLockView.setPinLockListener(mPinLockListener)

            mPinLockView.pinLength = 6
            mPinLockView.textColor = ContextCompat.getColor(this@PinLockActivity, R.color.white)
            mIndicatorDots.indicatorType = IndicatorDots.IndicatorType.FILL_WITH_ANIMATION

            pinLock.setOnClickListener {
                finishAffinity()
                val intent = Intent(this@PinLockActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private val mPinLockListener: PinLockListener = object : PinLockListener {
        override fun onComplete(pin: String) {
            verifyPIN(pin)
        }

        override fun onEmpty() {}
        override fun onPinChange(pinLength: Int, intermediatePin: String) {}
    }

    private fun verifyPIN(pin: String) {

        val user = FhirApplication.getProfile(this)
        val name = try {
            val it: User = gson.fromJson(user, User::class.java)
            it.email
        } catch (e: Exception) {
            "Unknown"
        }
        name.let { email ->
            apiService.loginUser(this@PinLockActivity, LoginData(email, pin)) {
                if (it != null) {
                    FhirApplication.updateDetails(this@PinLockActivity, it)
                    FhirApplication.setLoggedIn(this, true)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}