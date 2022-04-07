package com.intellisoft.nndak.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.databinding.ActivityLoginBinding
import com.intellisoft.nndak.utils.Common.isValidPassword
import com.intellisoft.nndak.utils.Common.validEmail
import com.intellisoft.nndak.utils.Common.validInput
import timber.log.Timber
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var signIn: TextView
    private lateinit var recover: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()

    }


    private fun initViews() {
        username = binding.eMail
        password = binding.ePass

        progressBar = binding.pbLoading
        hideProgress(progressBar, binding.btnSubmit)

        binding.btnSubmit.setOnClickListener {

            handleDataCheck()
        }


    }



    private fun handleDataCheck() {

        val user = username.text.toString()
        val pass = password.text.toString()
        if (!validInput(user)) {
            binding.eMail.error = getString(R.string.enter_email_address)
            binding.eMail.requestFocus()
            return
        }
        if (!validEmail(user)) {
            binding.eMail.error = getString(R.string.enter_valid_email_address)
            binding.eMail.requestFocus()
            return
        }
        if (isValidPassword(pass)) {
            // validateLogin(user, pass)
            FhirApplication.setLoggedIn(this, true)
            finishAffinity()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        } else {
            Toast.makeText(this, "Enter your 6 Digit Password", Toast.LENGTH_SHORT).show()
        }

    }

    private fun processAccountRecover(user: String) {
        showProgress(binding.pbLoading, binding.btnSubmit)
        val timer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Timber.d("It's just a matter of time")
            }

            override fun onFinish() {
                hideProgress(binding.pbLoading, binding.btnSubmit)
                startActivity(Intent(this@LoginActivity, OtpActivity::class.java))
            }
        }
        timer.start()

    }

    private fun validateLogin(user: String, pass: String) {

        showProgress(progressBar, binding.btnSubmit)
        val apiService = RestManager()
        val user = User(
            userId = null,
            userEmail = user,
            userPass = pass,
        )

        apiService.loginUser(user) {

            hideProgress(progressBar, binding.btnSubmit)

            if (it?.userId != null) {
                Timber.d("Success $it")
                FhirApplication.setLoggedIn(this, true)
                finishAffinity()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            } else {
                Timber.e("Error registering new user")
                Toast.makeText(this, "Error Encountered,please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun hideProgress(progressBar: ProgressBar, button: MaterialButton) {
        progressBar.isVisible = false
        button.isVisible = true
    }

    private fun showProgress(progressBar: ProgressBar, button: MaterialButton) {
        progressBar.isVisible = true
        button.isVisible = false
    }
}