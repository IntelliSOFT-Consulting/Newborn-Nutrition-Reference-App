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
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) {

            if (it != null) {
                binding.apply {
                    val gest = it.dashboard?.gestation ?: ""
                    val status = it.status
                    incDetails.tvBabyName.text = it.babyName
                    incDetails.tvMumName.text = it.motherName
                    incDetails.appBirthWeight.text = it.birthWeight
                    incDetails.appGestation.text = "$gest-$status"
                    incDetails.appApgarScore.text = it.dashboard?.apgarScore ?: ""
                    incDetails.appMumIp.text = it.motherIp
                    incDetails.appBabyWell.text = it.dashboard?.babyWell ?: ""
                    incDetails.appAsphyxia.text = it.dashboard?.asphyxia ?: ""
                    incDetails.appNeonatalSepsis.text = it.dashboard?.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = it.dashboard?.jaundice ?: ""
                    incDetails.appBirthDate.text = it.dashboard?.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = it.dashboard?.dayOfLife ?: ""
                    incDetails.appAdmDate.text = it.dashboard?.dateOfAdm ?: ""

                    /**
                     * Mum Details
                     */
                    incMum.tvMumName.text = it.motherName
                    incMum.appIpNumber.text = it.motherIp
                    incMum.appDeliveryMethod.text = it.mother.deliveryMethod
                    incMum.appParity.text = it.mother.parity
                    incMum.appPmctcStatus.text = it.mother.pmtctStatus
                    incMum.appDeliveryDate.text = it.mother.deliveryDate
                    incMum.appMultiplePregnancy.text = it.mother.multiPregnancy

                }
            }
        }

        binding.apply {
            actionProvideSupport.setOnClickListener {
                findNavController().navigate(BabyLactationFragmentDirections.navigateToEditPrescription(args.patientId))
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