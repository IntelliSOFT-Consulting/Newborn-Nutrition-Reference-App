package com.intellisoft.nndak.auth

import android.content.Intent
import android.os.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.data.LoginData
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.ActivityLoginBinding
import com.intellisoft.nndak.dialogs.ConnectionDialog
import com.intellisoft.nndak.utils.*
import org.hl7.fhir.r4.model.Flag
import timber.log.Timber
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var connectionDialog: ConnectionDialog
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

    private fun cancel() {
        connectionDialog.dismiss()
    }

    private fun initViews() {
        username = binding.eMail
        password = binding.ePass

        progressBar = binding.pbLoading
        hideProgress(progressBar, binding.btnSubmit)

        connectionDialog = ConnectionDialog(this::cancel)

        binding.apply {

            btnSubmit.setOnClickListener {

                handleDataCheck()
            }
            forgotPass.setOnClickListener {

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
                if (isNetworkAvailable(this@LoginActivity)) {
                    processAccountRecovery(user)
                } else {

                    connectionDialog.show(supportFragmentManager, "Confirm Details")
                }
            }
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

            if (isNetworkAvailable(this@LoginActivity)){
            validateLogin(user, pass)
            } else {

                connectionDialog.show(supportFragmentManager, "Confirm Details")
            }

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
                val message = it.message
                val alertDialog: AlertDialog =
                    this.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setTitle("Success")
                            setMessage(message)
                            setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                            }
                        }
                        builder.create()
                    }
                alertDialog.show()
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
                FhirApplication.updateDetails(this@LoginActivity, it)
                FhirApplication.setLoggedIn(this, true)
                finishAffinity()
                /*     if (it.newUser == true) {*/
                startActivity(
                    Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                /* }*/
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