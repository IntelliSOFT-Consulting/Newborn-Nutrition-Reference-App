package com.intellisoft.nndak.screens.dashboard.prescription

import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.BabyItemAdapter
import com.intellisoft.nndak.adapters.PrescriptionAdapter
import com.intellisoft.nndak.databinding.FragmentBabyFeedsBinding
import com.intellisoft.nndak.databinding.FragmentChildDashboardBinding
import com.intellisoft.nndak.models.PrescriptionItem
import com.intellisoft.nndak.screens.dashboard.BaseFragment
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BabyFeedsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabyFeedsFragment : Fragment() {
    private var _binding: FragmentBabyFeedsBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyFeedsFragmentArgs by navArgs()
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabyFeedsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_dashboard)

            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)



        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    requireActivity().application,
                    fhirEngine,
                    args.patientId
                )
            )
                .get(PatientDetailsViewModel::class.java)

        binding.apply {
            breadcrumb.page.text =
                Html.fromHtml("Babies > Baby Panel > <font color=\"#37379B\">Prescribe Feeds</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigateUp()
            }

        }
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { data ->

            if (data != null) {
                binding.apply {
                    val gest = data.dashboard.gestation ?: ""
                    val status = data.status
                    incDetails.tvBabyName.text = data.babyName
                    incDetails.tvMumName.text = data.motherName
                    incDetails.appBirthWeight.text = data.birthWeight
                    incDetails.appGestation.text = "$gest-$status"
                    incDetails.appApgarScore.text = data.dashboard.apgarScore ?: ""
                    incDetails.appMumIp.text = data.motherIp
                    incDetails.appBabyWell.text = data.dashboard.babyWell ?: ""
                    incDetails.appAsphyxia.text = data.dashboard.asphyxia ?: ""
                    incDetails.appNeonatalSepsis.text =
                        data.dashboard.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = data.dashboard.jaundice ?: ""
                    incDetails.appBirthDate.text = data.dashboard.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = data.dashboard.dayOfLife ?: ""
                    incDetails.appAdmDate.text = data.dashboard.dateOfAdm ?: ""

                    val isSepsis = data.dashboard.neonatalSepsis
                    val isAsphyxia = data.dashboard.asphyxia
                    val isJaundice = data.dashboard.jaundice
                    if (isSepsis == "Yes" || isAsphyxia == "Yes" || isJaundice == "Yes") {
                        incDetails.lnConditions.visibility = View.VISIBLE
                    }

                    if (isSepsis != "Yes") {
                        incDetails.appNeonatalSepsis.visibility = View.GONE
                        incDetails.tvNeonatalSepsis.visibility = View.GONE
                    }

                    if (isAsphyxia != "Yes") {
                        incDetails.appAsphyxia.visibility = View.GONE
                        incDetails.tvAsphyxia.visibility = View.GONE
                    }

                    if (isJaundice != "Yes") {
                        incDetails.tvJaundice.visibility = View.GONE
                        incDetails.appJaundice.visibility = View.GONE
                    }


                }
            }
        }

        /**
         * List of Prescriptions
         */
        val recyclerView: RecyclerView = binding.prescriptionList
        val adapter = PrescriptionAdapter(this::onPrescriptionItemClick)
        recyclerView.adapter = adapter

        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) {
            Timber.d("Prescriptions has " + it.count() + " records")
            if (it.isNotEmpty()) {
                binding.actionUpdatePrescription.visibility = View.VISIBLE
            }
            binding.pbLoading.visibility = View.GONE
            adapter.submitList(it)
        }

        binding.apply {
            actionNewPrescription.setOnClickListener {
                findNavController().navigate(
                    BabyFeedsFragmentDirections.navigateToAddPrescription(
                        args.patientId, "Add Prescription"
                    )
                )
            }
            actionUpdatePrescription.setOnClickListener {

                // Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    BabyFeedsFragmentDirections.navigateToAddPrescription(
                        args.patientId,
                        "Edit Prescription"
                    )
                )
            }
        }

    }

    private fun onPrescriptionItemClick(prescriptionItem: PrescriptionItem) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (requireActivity() as MainActivity).openNavigationDrawer()
                true
            }
            else -> false
        }
    }
}

