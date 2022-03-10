package com.imeja.demo.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.imeja.demo.FhirApplication
import com.imeja.demo.MainActivity
import com.imeja.demo.R
import com.imeja.demo.databinding.ActivityLoginBinding
import com.imeja.demo.utils.Notifications

class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressBar1: ProgressBar
    private lateinit var progressBar: ProgressBar
    private lateinit var materialButton: MaterialButton
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        observeChanges()

    }

    private fun observeChanges() {
        materialButton.isEnabled = !(username.text.isNullOrEmpty() || password.text.isNullOrEmpty())
    }

    private fun initViews() {
        username = binding.edtUsername
        password = binding.edtPassword

        materialButton = binding.loginButton

        progressBar1 = binding.locationLoadingProgressBar
        progressBar = binding.loginLoading
        hideProgress(progressBar1)
        hideProgress(progressBar)

        materialButton.setOnClickListener {

            handleDataCheck()
        }
        monitorChanges(username)
        monitorChanges(password)
    }

    private fun monitorChanges(text: TextInputEditText) {
        text.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                observeChanges()
            }
        })
    }

    private fun handleDataCheck() {
        val user = username.text.toString()
        val pass = password.text.toString()
        if (isValidPassword(pass)) {
            FhirApplication.setLoggedIn(this, true)
            finishAffinity()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        } else {
            Toast.makeText(this, "Enter your 6 Digit Password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidPassword(string: String): Boolean {
        if (string.length > 5) {
            return true
        }
        return false
    }

    private fun hideProgress(progressBar: ProgressBar) {
        progressBar.isVisible = false
    }
}