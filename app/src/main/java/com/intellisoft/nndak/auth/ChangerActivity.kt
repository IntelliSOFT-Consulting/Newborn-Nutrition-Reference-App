package com.intellisoft.nndak.auth

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.data.LoginData
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.databinding.ActivityChangerBinding
import com.intellisoft.nndak.utils.Common
import com.intellisoft.nndak.utils.Common.validInput
import timber.log.Timber

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.screen_encounter_fragment_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_patient_submit -> {
                onSubmitAction()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onSubmitAction() {
        /*Make APi Call Here*/
        val npass = binding.ePass.text.toString().trim()
        val cpass = binding.cPass.text.toString().trim()

        if (!validInput(npass)) {
            binding.ePass.error = getString(R.string.action_enter_pin)
            binding.ePass.requestFocus()
            return
        }
        if (!validInput(cpass)) {
            binding.cPass.error = getString(R.string.action_confirm_password)
            binding.cPass.requestFocus()
            return
        }
        if (npass != cpass) {
            binding.cPass.error = getString(R.string.action_matching_password)
            binding.cPass.requestFocus()
            return
        }
        val apiService = RestManager()
        val user = LoginData(
            email = npass,
            password = cpass,
        )
        binding.progressBar.isVisible = true
        apiService.loginUser(this,user) {
            binding.progressBar.isVisible = false
            if (it != null) {
                Timber.d("Success $it")
                finishAffinity()
                startActivity(Intent(this@ChangerActivity, MainActivity::class.java))
            } else {
                Timber.e("Error registering new user")
                Toast.makeText(this, "Error Encountered,please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = ""
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

}


