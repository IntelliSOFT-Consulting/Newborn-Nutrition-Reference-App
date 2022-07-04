package com.intellisoft.nndak.screens.dashboard.prescription

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.FeedingAdapter
import com.intellisoft.nndak.adapters.PrescriptionAdapter
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.ActivityHistoryBinding
import com.intellisoft.nndak.models.PrescriptionItem
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var careId: String
    private lateinit var patientId: String
    private val apiService = RestManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Prescription History"
            setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
        }
        patientId = intent.getStringExtra("patientId").toString()
        careId = intent.getStringExtra("careId").toString()

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    application,
                    fhirEngine,
                    patientId
                )
            ).get(PatientDetailsViewModel::class.java)

        val recyclerView: RecyclerView = binding.prescriptionList
        val adapter = FeedingAdapter()
        recyclerView.adapter = adapter

        patientDetailsViewModel.getFeedingInstances(careId)
        patientDetailsViewModel.liveFeedingData.observe(this) {
            if (it.isNullOrEmpty()) {
                promptUser()
                binding.pbLoadingTwo.visibility = View.GONE
            }

            binding.pbLoadingTwo.visibility = View.GONE
            adapter.submitList(it)
        }
    }

    private fun promptUser() {
        val dialog =
            SweetAlertDialog(this@HistoryActivity, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Error")
                .setContentText(resources.getString(R.string.empty_hist))
                .setCustomImage(R.drawable.crying)
                .setConfirmClickListener { sDialog ->
                    run {
                        sDialog.dismiss()
                      this@HistoryActivity.finish()
                    }
                }
        dialog.setCancelable(false)
        dialog.show()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


}

