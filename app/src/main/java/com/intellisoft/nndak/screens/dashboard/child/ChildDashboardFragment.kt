package com.intellisoft.nndak.screens.dashboard.child

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R 
import com.intellisoft.nndak.databinding.FragmentChildDashboardBinding
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

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

        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) {

            if (it != null) {
                binding.apply {
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

            if (!args.active) {
                lnBabyDashboard.isEnabled = false
                lnBabyFeeding.isEnabled = false
                lnBabyMonitoring.isEnabled = false
                lnBabyLactation.isEnabled = false
                dimOption(imgDashboard)
                dimOption(imgFeeds)
                dimOption(imgMonitor)
                dimOption(imgLactation)
            }
            lnBabyDashboard.setOnClickListener {
                findNavController().navigate(ChildDashboardFragmentDirections.navigateToBabyDashboard(args.patientId))
            }
            lnBabyAssessment.setOnClickListener {
                findNavController().navigate(ChildDashboardFragmentDirections.navigateToBabyAssessment(args.patientId))
            }
            lnBabyFeeding.setOnClickListener {
                findNavController().navigate(ChildDashboardFragmentDirections.navigateToBabyFeeding(args.patientId))
            }
            lnBabyMonitoring.setOnClickListener {
                findNavController().navigate(ChildDashboardFragmentDirections.navigateToBabyMonitoring(args.patientId))
            }
            lnBabyLactation.setOnClickListener {
                findNavController().navigate(ChildDashboardFragmentDirections.navigateToBabyLactation(args.patientId))
            }
        }

    }

    private fun dimOption(imageView: ImageView) {
        ImageViewCompat.setImageTintMode(imageView, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(
            imageView,
            ColorStateList.valueOf(Color.parseColor("#94C4C4C4"))
        )

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