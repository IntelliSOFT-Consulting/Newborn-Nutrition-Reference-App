package com.intellisoft.nndak.auth

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.nndak.databinding.ActivityOtpBinding


class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {

        listenForward(binding.etC1,binding.etC2)
        listenForward(binding.etC2,binding.etC3)
        listenForward(binding.etC3,binding.etC4)
        listenForward(binding.etC4,binding.etC5)
        listenForward(binding.etC5,binding.etC6)
        listenForward(binding.etC6,binding.etC6)

        listenBackward(binding.etC2,binding.etC1)
    }

    private fun listenForward(current: EditText, next: EditText) {
        current.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val text: Int = current.text.toString().length
                if (text == 1) {
                    next.requestFocus();
                }
            }

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        })
    }

    private fun listenBackward(current: EditText, previous: EditText) {

    }
}