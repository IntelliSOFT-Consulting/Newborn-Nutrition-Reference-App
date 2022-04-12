package com.intellisoft.nndak.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.ActivityChangerBinding
import timber.log.Timber

class ChangerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangerBinding
    private lateinit var mPinLockView: PinLockView
    private lateinit var mIndicatorDots: IndicatorDots


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

        mPinLockView = binding.pinLockView
        mIndicatorDots = binding.indicatorDots

        mPinLockView.attachIndicatorDots(mIndicatorDots)
        mPinLockView.setPinLockListener(pinLock)
        mPinLockView.pinLength = 6
        mPinLockView.textColor = ContextCompat.getColor(this@ChangerActivity, R.color.white)

    }

//


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


