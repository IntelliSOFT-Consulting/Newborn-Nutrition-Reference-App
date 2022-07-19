package com.intellisoft.nndak.screens.dashboard.prescription

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.PrescriptionAdapter
import com.intellisoft.nndak.databinding.FragmentBabyFeedsBinding
import com.intellisoft.nndak.logic.Logics.Companion.ADMINISTRATOR
import com.intellisoft.nndak.logic.Logics.Companion.DOCTOR
import com.intellisoft.nndak.logic.Logics.Companion.NEONATOLOGIST
import com.intellisoft.nndak.logic.Logics.Companion.PEDIATRICIAN
import com.intellisoft.nndak.models.PrescriptionItem
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
    private lateinit var careId: String
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
            incDetails.pbLoading.visibility = View.VISIBLE
            incDetails.lnBody.visibility = View.GONE
        }

        binding.apply {
            breadcrumb.page.text =
                Html.fromHtml("Babies > Baby Panel > <font color=\"#37379B\">Prescribe Feeds</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigateUp()
            }

        }
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { data ->

            if (data != null) {
                binding.apply {

                    incDetails.pbLoading.visibility = View.GONE
                    incDetails.lnBody.visibility = View.VISIBLE

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

        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) {
            Timber.d("Prescriptions has " + it.count() + " records")
            if (it.isNotEmpty()) {
                binding.actionUpdatePrescription.visibility = View.VISIBLE
                binding.tvHeader.visibility = View.VISIBLE
                val value = it.first().resourceId.toString()
                careId = value
                Timber.e("Found Reference $careId")
                adapter.submitList(it.subList(0, 1))
            }
            if (it.isEmpty()) {
                binding.incEmpty.cpBgView.visibility = View.VISIBLE
                binding.incEmpty.cpTitle.text = getString(R.string.add_pres)
            }

            binding.pbLoadingTwo.visibility = View.GONE
            binding.incEmpty.cpBgView.visibility = View.GONE
            binding.incEmpty.cpTitle.visibility = View.GONE

        }

        binding.apply {

            val allowed = validatePermission()
            actionNewPrescription.setOnClickListener {

                if (allowed) {
                    findNavController().navigate(
                        BabyFeedsFragmentDirections.navigateToAddPrescription(
                            args.patientId, "Add Prescription"
                        )
                    )
                } else {
                    notifyUser()
                }
            }
            actionUpdatePrescription.setOnClickListener {

                if (allowed) {
                    findNavController().navigate(
                        BabyFeedsFragmentDirections.navigateToEditPrescription(
                            args.patientId, careId
                        )
                    )
                } else {
                    notifyUser()
                }
            }
        }

    }

    private fun notifyUser() {
        SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
            .setTitleText("Access Denied!!")
            .setContentText("You are not Authorized")
            .setCustomImage(R.drawable.smile)
            .show()
    }

    private fun validatePermission(): Boolean {

        val role = (requireActivity() as MainActivity).retrieveUser(true)
        if (role.isNotEmpty()) {
            return role == ADMINISTRATOR || role == DOCTOR || role == NEONATOLOGIST || role == PEDIATRICIAN
        }
        return false
    }

    private fun onPrescriptionItemClick(item: PrescriptionItem) {
     /*   startActivity(
            Intent(requireContext(), HistoryActivity::class.java)
                .putExtra("careId", item.resourceId)
                .putExtra("patientId", args.patientId)
        )*/

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (requireActivity() as MainActivity).openNavigationDrawer()
                true
            }
            R.id.menu_profile -> {
                (requireActivity() as MainActivity).navigate(R.id.profileFragment)
                return true
            }
            else -> false
        }
    }
}

