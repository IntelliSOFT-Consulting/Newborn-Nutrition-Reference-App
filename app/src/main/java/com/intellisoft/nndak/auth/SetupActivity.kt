package com.intellisoft.nndak.auth

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.ActivitySetupBinding
import com.intellisoft.nndak.utils.hideProgress
import com.intellisoft.nndak.utils.isValidURL
import com.intellisoft.nndak.utils.showProgress
import com.intellisoft.nndak.utils.validInput
import kotlin.system.exitProcess

class SetupActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetupBinding.inflate(layoutInflater)
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
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.btnSubmit.setOnClickListener {


            val url = binding.eServer.text.toString().trim()
            val demo = binding.eDemo.text.toString().trim()

            if (!validInput(url)) {
                binding.eServer.error = getString(R.string.enter_server_url)
                binding.eServer.requestFocus()
                return@setOnClickListener
            }
            if (!isValidURL(url)) {
                binding.eServer.error = getString(R.string.enter_valid_server_url)
                binding.eServer.requestFocus()
                return@setOnClickListener
            }

            /***
             * Demo FHIR URL
             * */

            if (!validInput(demo)) {
                binding.eDemo.error = getString(R.string.enter_server_url)
                binding.eDemo.requestFocus()
                return@setOnClickListener
            }
            if (!isValidURL(demo)) {
                binding.eDemo.error = getString(R.string.enter_valid_server_url)
                binding.eDemo.requestFocus()
                return@setOnClickListener
            }
            reLaunchApp(url, demo)
        }

    }

    private fun reLaunchApp(url: String, demo: String) {

        FhirApplication.setServerDetails(this, true, url, demo)
        showProgress(binding.pbLoading)
        val timer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                hideProgress(binding.pbLoading)
                val intent = Intent(baseContext, SplashActivity::class.java)
                val pendingIntentId = 101
                val pendingIntent = PendingIntent.getActivity(
                    this@SetupActivity,
                    pendingIntentId,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                val alarmManager =
                    (this@SetupActivity.getSystemService(Context.ALARM_SERVICE)) as AlarmManager
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
                exitProcess(0)
            }
        }
        timer.start()

    }
}