package com.intellisoft.nndak.screens.dashboard

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import ca.uhn.fhir.context.FhirContext
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentRegistrationBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.screens.custom.CustomQuestionnaireFragment
import com.intellisoft.nndak.utils.generateUuid
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class RegistrationFragment : Fragment() {
    private lateinit var confirmationDialog: ConfirmationDialog
    private lateinit var patientId: String
    private var _binding: FragmentRegistrationBinding? = null
    private val viewModel: ScreenerViewModel by viewModels()
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.home_client)
            setDisplayHomeAsUpEnabled(true)
        }
        updateArguments()
        onBackPressed()
        observeResourcesSaveAction()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        setHasOptionsMenu(true)
        binding.apply {

            breadcrumb.page.text =
                Html.fromHtml("Babies > <font color=\"#37379B\">Client Registration</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigate(RegistrationFragmentDirections.navigateToLanding())
            }
            btnSubmit.setOnClickListener {
                onSubmitAction()
            }
            btnCancel.setOnClickListener {
                showCancelScreenerQuestionnaireAlertDialog()
            }
        }
        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_confirm_message)
        )

        patientId = generateUuid()

    }

    private fun okClick() {
        confirmationDialog.dismiss()
        (activity as MainActivity).displayDialog()

        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

            patientId = generateUuid()
            viewModel.clientRegistration(
                questionnaireFragment.getQuestionnaireResponse(), patientId
            )
        }
    }


    private fun observeResourcesSaveAction() {
        viewModel.customMessage.observe(viewLifecycleOwner) {

            if (it != null) {
                if (it.success) {
                    (activity as MainActivity).hideDialog()

                    val dialog =
                        SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                            .setTitleText("Success")
                            .setContentText(resources.getString(R.string.app_client_registered))
                            .setCustomImage(R.drawable.smile)
                            .setConfirmClickListener { sDialog ->
                                run {
                                    sDialog.dismiss()
                                    FhirApplication.setDashboardActive(requireContext(), false)
                                    findNavController().navigate(
                                        RegistrationFragmentDirections.navigateToBabyDashboard(
                                            patientId
                                        )
                                    )
                                }
                            }


                    dialog.setCancelable(false)
                    dialog.show()
                } else {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                    (activity as MainActivity).hideDialog()
                    return@observe
                }
            }
            (activity as MainActivity).hideDialog()

        }
    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "client-registration.json")
  }

    private fun addQuestionnaireFragment() {
        try {
            val fragment = CustomQuestionnaireFragment()
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
        val alertDialog: AlertDialog? =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.cancel_questionnaire_message))
                    setPositiveButton(getString(R.string.yes)) { _, _ ->
                        NavHostFragment.findNavController(this@RegistrationFragment).navigateUp()
                    }
                    setNegativeButton(getString(R.string.no)) { _, _ -> }
                }
                builder.create()
            }
        alertDialog?.show()
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