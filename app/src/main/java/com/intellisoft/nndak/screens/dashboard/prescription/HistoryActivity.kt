package com.intellisoft.nndak.screens.dashboard.prescription

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.FeedingAdapter
import com.intellisoft.nndak.adapters.PrescriptionAdapter
import com.intellisoft.nndak.databinding.ActivityHistoryBinding
import com.intellisoft.nndak.models.PrescriptionItem
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var careId: String
    private lateinit var patientId: String

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
                binding.imgNothing.visibility=View.VISIBLE
                binding.pbLoadingTwo.visibility = View.GONE
            }

            binding.pbLoadingTwo.visibility = View.GONE
            adapter.submitList(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun onPrescriptionItemClick(data: PrescriptionItem) {

    }
}

