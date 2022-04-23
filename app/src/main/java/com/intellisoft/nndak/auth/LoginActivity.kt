package com.intellisoft.nndak.auth

import android.content.Intent
import android.os.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.data.LoginData
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.ActivityLoginBinding
import com.intellisoft.nndak.utils.*
import timber.log.Timber
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private var doubleBackToExitPressedOnce = false
    private val apiService = RestManager()

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

            startActivity(Intent(this@LoginActivity, MainActivity::class.java))

            handleDataCheck()
        }
        binding.forgotPass.setOnClickListener {

            val user = username.text.toString().trim()

            if (!validInput(user)) {
                binding.eMail.error = getString(R.string.enter_email_address)
                binding.eMail.requestFocus()
                return@setOnClickListener
            }
            if (!validEmail(user)) {
                binding.eMail.error = getString(R.string.enter_valid_email_address)
                binding.eMail.requestFocus()
                return@setOnClickListener
            }
            processAccountRecovery(user)
        }
    }


    private fun handleDataCheck() {

        val user = username.text.toString().trim()
        val pass = password.text.toString().trim()
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
            validateLogin(user, pass)

        } else {
            Toast.makeText(this, "Enter your 6 Digit Password", Toast.LENGTH_SHORT).show()
        }

    }

    private fun processAccountRecovery(email: String) {
        showProgress(binding.pbLoading, binding.btnSubmit)

        val user = LoginData(email = email, password = null)
        apiService.resetPassword(this, user) {

            hideProgress(progressBar, binding.btnSubmit)

            if (it != null) {
                Timber.d("Success $it")
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid Credentials, please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }

    private fun validateLogin(email: String, pass: String) {

        showProgress(progressBar, binding.btnSubmit)

        val user = LoginData(email = email, password = pass)
        apiService.loginUser(this, user) {

            hideProgress(progressBar, binding.btnSubmit)

            if (it != null) {
                Timber.d("Success $it")
                FhirApplication.updateDetails(this@LoginActivity, it)
                FhirApplication.setLoggedIn(this, true)
                finishAffinity()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Invalid Credentials, please try again", Toast.LENGTH_SHORT)
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

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}