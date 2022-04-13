package com.intellisoft.nndak.screens.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.auth.LoginActivity
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.databinding.ActivityProfileBinding


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
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
        loadAccountDetails()
        binding.lytLogout.setOnClickListener {
            confirmLogout()
        }

    }

    private fun confirmLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            FhirApplication.setLoggedIn(this, false)
            finishAffinity()
            val i = Intent(this@ProfileActivity, LoginActivity::class.java)
            startActivity(i)
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun loadAccountDetails() {
        val user = FhirApplication.getProfile(this)
        if (user != null) {
            val gson = Gson()
            val it: User = gson.fromJson(user, User::class.java)
            binding.tvName.text = it.names
            binding.tvEmail.text = it.email
            binding.tvRole.text = it.role
            binding.tvSince.text = it.createdAt.substring(0, 10)
        }
    }

}