package com.intellisoft.nndak.screens.dashboard.child

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBabyLactationBinding
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
 * Use the [BabyLactationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabyLactationFragment : Fragment() {
    private var _binding: FragmentBabyLactationBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyAssessmentFragmentArgs by navArgs()
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabyLactationBinding.inflate(inflater, container, false)
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
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { motherBabyItem ->

            if (motherBabyItem != null) {
                binding.apply {
                    val gest = motherBabyItem.dashboard.gestation ?: ""
                    val status = motherBabyItem.status
                    incDetails.tvBabyName.text = motherBabyItem.babyName
                    incDetails.tvMumName.text = motherBabyItem.motherName
                    incDetails.appBirthWeight.text = motherBabyItem.birthWeight
                    incDetails.appGestation.text = "$gest-$status"
                    incDetails.appApgarScore.text = motherBabyItem.dashboard.apgarScore ?: ""
                    incDetails.appMumIp.text = motherBabyItem.motherIp
                    incDetails.appBabyWell.text = motherBabyItem.dashboard.babyWell ?: ""
                    incDetails.appAsphyxia.text = motherBabyItem.dashboard.asphyxia ?: ""
                    incDetails.appNeonatalSepsis.text =
                        motherBabyItem.dashboard.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = motherBabyItem.dashboard.jaundice ?: ""
                    incDetails.appBirthDate.text = motherBabyItem.dashboard.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = motherBabyItem.dashboard.dayOfLife ?: ""
                    incDetails.appAdmDate.text = motherBabyItem.dashboard.dateOfAdm ?: ""

                    /**
                     * Mum Details
                     */
                    incMum.tvMumName.text = motherBabyItem.motherName
                    incMum.appIpNumber.text = motherBabyItem.motherIp
                    incMum.appDeliveryMethod.text = motherBabyItem.mother.deliveryMethod
                    incMum.appParity.text = motherBabyItem.mother.parity
                    incMum.appPmctcStatus.text = motherBabyItem.mother.pmtctStatus
                    incMum.appDeliveryDate.text = motherBabyItem.mother.deliveryDate
                    incMum.appMultiplePregnancy.text = motherBabyItem.mother.multiPregnancy

                }
            }
        }

        binding.apply {
            actionProvideSupport.setOnClickListener {
                findNavController().navigate(BabyLactationFragmentDirections.navigateToFeeding(args.patientId))
            }
        }

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