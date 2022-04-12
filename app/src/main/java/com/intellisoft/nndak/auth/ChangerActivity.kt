package com.intellisoft.nndak.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.ActivityChangerBinding
import timber.log.Timber

import com.andrognito.pinlockview.PinLockListener

class ChangerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun verifyPIN(pin: String) {
        Timber.d("Pin Entered $pin")

    }

    private fun initViews() {
        binding.pinLockView.attachIndicatorDots(binding.indicatorDots)
        binding.pinLockView.setPinLockListener(pinLock)
        binding.pinLockView.pinLength=6
        binding.pinLockView.textColor = ContextCompat.getColor(this@ChangerActivity, R.color.white)

    }

    private val pinLock: PinLockListener = object : PinLockListener {

        override fun onComplete(pin: String) {
            verifyPIN(pin)
        }

        override fun onEmpty() {
            Timber.d("Pin Empty")
        }

        override fun onPinChange(pinLength: Int, intermediatePin: String) {
            Timber.d("Pin Entered $intermediatePin")
        }
    }
}


