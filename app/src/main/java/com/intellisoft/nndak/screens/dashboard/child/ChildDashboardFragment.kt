package com.intellisoft.nndak.screens.dashboard.child

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentChildDashboardBinding
import com.intellisoft.nndak.logic.Logics.Companion.HMB_ASSISTANT
import com.intellisoft.nndak.utils.dimOption
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChildDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChildDashboardFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private var _binding: FragmentChildDashboardBinding? = null
    var editMenuItem: MenuItem? = null
    private val binding
        get() = _binding!!

    private val args: ChildDashboardFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_dashboard)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        onBackPressed()
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

        binding.apply {
            incDetails.lnBody.visibility = View.GONE
            incDetails.pbLoading.visibility = View.VISIBLE
            breadcrumb.page.text =
                Html.fromHtml("Babies > <font color=\"#37379B\">Baby Profile</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigateUp()
            }
        }

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

        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) {

            if (it != null) {
                editMenuItem?.isEnabled = true
                binding.apply {
                    incDetails.lnBody.visibility = View.VISIBLE
                    incDetails.pbLoading.visibility = View.GONE

                    incDetails.appBabyName.text = it.babyName
                    incDetails.appMotherName.text = it.motherName
                    incDetails.appBirthWeight.text = it.birthWeight
                    incDetails.appStatus.text = it.status
                    incDetails.appRateGain.text = it.gainRate
                    incDetails.appIpNumber.text = it.motherIp

                    if (it.status == "Term") {
                        incDetails.appStatus.setTextColor(resources.getColor(R.color.dim_green))
                    }
                }
            }
        }


        binding.apply {
            val isActive = FhirApplication.getDashboardActive(requireContext())
            val allowed = validatePermission()
            if (!isActive) {
                lnBabyDashboard.isEnabled = false
                lnBabyFeeding.isEnabled = false
                lnBabyMonitoring.isEnabled = false
                lnBabyLactation.isEnabled = false
                dimOption(imgDashboard, "#94C4C4C4")
                dimOption(imgFeeds, "#94C4C4C4")
                dimOption(imgMonitor, "#94C4C4C4")
                dimOption(imgLactation, "#94C4C4C4")
            }
            lnBabyDashboard.setOnClickListener {
                findNavController().navigate(
                    ChildDashboardFragmentDirections.navigateToBabyDashboard(
                        args.patientId
                    )
                )
            }
            lnBabyAssessment.setOnClickListener {
                findNavController().navigate(
                    ChildDashboardFragmentDirections.navigateToBabyAssessment(
                        args.patientId
                    )
                )
            }
            lnBabyFeeding.setOnClickListener {
                findNavController().navigate(
                    ChildDashboardFragmentDirections.navigateToBabyFeeding(
                        args.patientId
                    )
                )
            }
            lnBabyMonitoring.setOnClickListener {
                if (allowed) {
                    findNavController().navigate(
                        ChildDashboardFragmentDirections.navigateToBabyMonitoring(
                            args.patientId
                        )
                    )
                } else {
                    accessDenied()
                }
            }
            lnBabyLactation.setOnClickListener {
                findNavController().navigate(
                    ChildDashboardFragmentDirections.navigateToBabyLactation(
                        args.patientId
                    )
                )
            }
        }

    }

    private fun validatePermission(): Boolean {

        val role = (requireActivity() as MainActivity).retrieveUser(true)
        if (role.isNotEmpty()) {
            return role != HMB_ASSISTANT
        }
        return false
    }

    private fun accessDenied() {
        SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
            .setTitleText("Access Denied!!")
            .setContentText("You are not Authorized")
            .setCustomImage(R.drawable.smile)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.details_options_menu, menu)
        editMenuItem = menu.findItem(R.id.menu_patient_edit)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (requireActivity() as MainActivity).openNavigationDrawer()
                true
            }
            R.id.menu_patient_edit -> {
                findNavController()
                    .navigate(ChildDashboardFragmentDirections.navigateToEditBaby(args.patientId))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            findNavController().navigate(ChildDashboardFragmentDirections.navigateToBabiesPanel())
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}