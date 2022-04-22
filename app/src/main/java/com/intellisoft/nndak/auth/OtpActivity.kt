package com.intellisoft.nndak.auth

import android.R
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.intellisoft.nndak.databinding.ActivityOtpBinding
import com.intellisoft.nndak.utils.*


class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {

        listenForward(binding.etC1, binding.etC2)
        listenForward(binding.etC2, binding.etC3)
        listenForward(binding.etC3, binding.etC4)
        listenForward(binding.etC4, binding.etC5)
        listenForward(binding.etC5, binding.etC6)
        listenForward(binding.etC6, binding.etC6)


        binding.btnVerify.setOnClickListener {
            if (!validInput(binding.etC1.text.toString()) || !validInput(binding.etC2.text.toString()) ||
                !validInput(binding.etC3.text.toString()) || !validInput(binding.etC4.text.toString()) ||
                !validInput(binding.etC5.text.toString()) || !validInput(
                    binding.etC6.text.toString()
                )
            ) {
                Toast.makeText(this@OtpActivity, "Please enter code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            finish()
            startActivity(Intent(this@OtpActivity, ChangerActivity::class.java))
        }
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


}