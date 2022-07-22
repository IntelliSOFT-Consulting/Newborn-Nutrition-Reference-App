package com.intellisoft.nndak.screens.dashboard.dhm

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ca.uhn.fhir.context.FhirContext
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.ItemOrder
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.FragmentProcessOrderBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.utils.boldText
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.android.synthetic.main.success_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProcessOrderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProcessOrderFragment : Fragment() {
    private lateinit var confirmationDialog: ConfirmationDialog
    private var _binding: FragmentProcessOrderBinding? = null
    private val viewModel: ScreenerViewModel by viewModels()
    private val apiService = RestManager()
    private var dhmType: String = ""
    private lateinit var order: ItemOrder
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProcessOrderBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_dhm_orders)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        updateArguments()
        onBackPressed()
        observeResourcesSaveAction()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        setHasOptionsMenu(true)

        val data = FhirApplication.getOrder(requireContext())
        if (data != null) {
            val gson = Gson()
            try {
                val it: ItemOrder = gson.fromJson(data, ItemOrder::class.java)
                order = it
                updateUI(it)

            } catch (e: Exception) {
            }
        }


        binding.apply {

            /**
             * Bold Title Texts
             */
            boldText(binding.incTitle.appMotherName)
            boldText(binding.incTitle.appIpNumber)
            boldText(binding.incTitle.appBabyName)
            boldText(binding.incTitle.appBabyAge)
            boldText(binding.incTitle.appDhmType)
            screen.btnSubmit.setOnClickListener {
                onSubmitAction()
            }
            screen.btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_okay_message)
        )


    }

    private fun updateUI(it: ItemOrder) {

        binding.apply {
            dhmType = it.dhmType
            incTitle.lnParent.weightSum = 5F
            incDetails.appBabyName.text = it.babyName
            incDetails.appMotherName.text = it.motherName
            incDetails.appIpNumber.text = it.motherIp
            incDetails.appBabyAge.text = it.babyAge
            incDetails.appDhmType.text = it.dhmType
            incDetails.appConsent.text = it.consentGiven
            appDhmReason.text = it.dhmReason
            appDhmVolume.text = it.dhmVolume

            incDetails.appBabyAge.visibility = View.VISIBLE
            incDetails.appAction.visibility = View.GONE
            incDetails.lnAction.visibility = View.GONE
            incDetails.appConsent.visibility = View.GONE
            incDetails.lnParent.weightSum = 5F


            /** Hide Titles */

            incTitle.appBabyAge.visibility = View.VISIBLE
            incTitle.appAction.visibility = View.GONE
            incTitle.lnAction.visibility = View.GONE
            incTitle.appConsent.visibility = View.GONE
            incTitle.lnParent.weightSum = 5F
        }

    }

    private fun okClick() {
        confirmationDialog.dismiss()
        (activity as MainActivity).displayDialog()

        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

            viewModel.liveDispensing(
                questionnaireFragment.getQuestionnaireResponse(),
                requireContext(),
                apiService,
                order,
                dhmType
            )
        }

    }


    private fun observeResourcesSaveAction() {
        viewModel.customMessage.observe(viewLifecycleOwner) {
            if (!it.success) {
                SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(it.message)
                    .setConfirmClickListener { dialog ->
                        run {
                            dialog.dismiss()
                            findNavController().navigateUp()
                        }
                    }.show()
                (activity as MainActivity).hideDialog()
                return@observe
            }
            (activity as MainActivity).hideDialog()
            val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Success")
                .setContentText(it.message)
                .setCustomImage(R.drawable.smile)
                .setConfirmClickListener { sDialog ->
                    run {
                        sDialog.dismiss()
                        findNavController().navigateUp()
                    }
                }
            dialog.setCancelable(false)
            dialog.show()
        }
    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "process-order.json")
    }

    private fun addQuestionnaireFragment() {
        try {
            val fragment = QuestionnaireFragment()
            fragment.arguments =
                bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
            childFragmentManager.commit {
                add(
                    R.id.add_patient_container, fragment,
                    QUESTIONNAIRE_FRAGMENT_TAG
                )
            }
        } catch (e: Exception) {
            Timber.e("Exception ${e.localizedMessage}")
        }
    }

    private fun onSubmitAction() {

        confirmationDialog.show(childFragmentManager, "Confirm Details")

    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {

        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText(getString(R.string.cancel_questionnaire_message))
            .setConfirmText("Yes")
            .setConfirmClickListener { d ->
                d.dismiss()
                NavHostFragment.findNavController(this@ProcessOrderFragment).navigateUp()
            }
            .setCancelText("No")
            .show()
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
    }

    override fun onResume() {

        (requireActivity() as MainActivity).showBottomNavigationView(View.GONE)
        super.onResume()
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

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}