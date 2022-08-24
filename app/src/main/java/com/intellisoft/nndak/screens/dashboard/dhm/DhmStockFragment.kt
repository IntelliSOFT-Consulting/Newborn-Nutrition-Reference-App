package com.intellisoft.nndak.screens.dashboard.dhm

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import ca.uhn.fhir.context.FhirContext
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.data.DHMStock
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.data.Stock
import com.intellisoft.nndak.databinding.FragmentDhmStockBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.models.CodingObservation
import com.intellisoft.nndak.utils.disableEditing
import com.intellisoft.nndak.utils.listenPlainChanges
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
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
 * Use the [DhmStockFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DhmStockFragment : Fragment() {
    private lateinit var confirmationDialog: ConfirmationDialog
    private var _binding: FragmentDhmStockBinding? = null
    private val viewModel: ScreenerViewModel by viewModels()
    private lateinit var pa: String
    private lateinit var upa: String
    private lateinit var totalDhm: String
    private lateinit var prepa: String
    private lateinit var preupa: String
    private lateinit var pretotalDhm: String
    val stockList = ArrayList<CodingObservation>()
    private val apiService = RestManager()
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDhmStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_dhm_stock)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        updateArguments()
        onBackPressed()
        observeResourcesSaveAction()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        binding.apply {

            breadcrumb.page.text =
                Html.fromHtml("HMB > <font color=\"#37379B\">Add Stock</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigateUp()
            }

            listenToChange(edPasteurized)
            listenToChange(edUnpasteurized)
            listenToChange(edPrePasteurized)
            listenToChange(edPreUnpasteurized)
            listenMaxChanges(edPasteurized, tilPasteurized, "Enter value", 1, 50000)
            listenMaxChanges(edUnpasteurized, tilUnpasteurized, "Enter amount", 1, 50000)
            listenMaxChanges(edPrePasteurized, tilPrePasteurized, "Enter value", 1, 50000)
            listenMaxChanges(edPreUnpasteurized, tilPreUnpasteurized, "Enter amount", 1, 50000)
            disableEditing(edTotal)
            disableEditing(edPreTotal)


            btnSubmit.setOnClickListener {
                onSubmitAction()
            }
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_okay_message)
        )


    }

    private fun listenMaxChanges(
        input: TextInputEditText,
        inputLayout: TextInputLayout,
        error: String,
        min: Int,
        max: Int
    ) {

        CoroutineScope(Dispatchers.Default).launch {
            input.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(editable: Editable) {
                    try {
                        if (editable.toString().isNotEmpty()) {
                            val newValue = editable.toString()
                            input.removeTextChangedListener(this)
                            val position: Int = input.selectionEnd
                            input.setText(newValue)
                            if (position > (input.text?.length ?: 0)) {
                                input.text?.let { input.setSelection(it.length) }
                            } else {
                                input.setSelection(position);
                            }

                            input.addTextChangedListener(this)
                            if (input.text.toString().isNotEmpty()) {
                                val parsed = newValue.toDouble()
                                val minimum = min.toDouble()
                                val maximum = max.toDouble()
                                if (parsed < minimum) {
                                    inputLayout.error = "Minimum allowed is $minimum"
                                } else if (parsed > maximum) {
                                    inputLayout.error = "Maximum allowed is $maximum"
                                } else {
                                    inputLayout.error = null
                                }
                            } else {
                                inputLayout.error = null
                            }
                        } else {
                            inputLayout.error = error
                        }
                    } catch (e: Exception) {

                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {

                }
            })
        }
    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "dhm-stock.json")

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

    override fun onResume() {

        (requireActivity() as MainActivity).showBottomNavigationView(View.GONE)
        super.onResume()
    }

    private fun listenToChange(input: TextInputEditText) {

        CoroutineScope(Dispatchers.Default).launch {
            input.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable) {
                    try {
                        if (editable.toString().isNotEmpty()) {
                            val newValue = editable.toString()
                            input.removeTextChangedListener(this)
                            val position: Int = input.selectionEnd
                            input.setText(newValue)
                            if (position > (input.text?.length ?: 0)) {
                                input.text?.let { input.setSelection(it.length) }
                            } else {
                                input.setSelection(position)
                            }
                            input.addTextChangedListener(this)

                            calculateTotals()
                        } else {
                            input.setText("0")
                            calculateTotals()
                        }
                    } catch (e: Exception) {

                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {

                }
            })
        }
    }

    private fun calculateTotals() {
        try {
            binding.apply {

                /**
                 * Calculate total Term
                 */
                pa = edPasteurized.text.toString()
                upa = edUnpasteurized.text.toString()
                if (pa.isNotEmpty() && upa.isNotEmpty()) {
                    val total = pa.toFloat() + upa.toFloat()

                    totalDhm = total.toString()

                    edTotal.setText(total.toString())
                } else {
                    edTotal.setText("0")
                }

                /**
                 * Calculate total Preterm
                 */
                prepa = edPrePasteurized.text.toString()
                preupa = edPreUnpasteurized.text.toString()
                if (prepa.isNotEmpty() && preupa.isNotEmpty()) {
                    val total = prepa.toFloat() + preupa.toFloat()

                    pretotalDhm = total.toString()

                    edPreTotal.setText(total.toString())
                } else {
                    edPreTotal.setText("0")
                }

            }

        } catch (e: Exception) {
            Timber.e("Exception::: ${e.localizedMessage}")
        }

    }

    private fun okClick() {
        confirmationDialog.dismiss()
        (activity as MainActivity).displayDialog()
        val userId = (activity as MainActivity).retrieveUser(false)

        val stock = DHMStock(
            term = Stock(
                pasteurized = pa,
                unPasteurized = upa,
                total = "0"
            ),
            preterm = Stock(
                pasteurized = prepa,
                unPasteurized = preupa,
                total = "0"
            ),
            userId = userId
        )

        Timber.e("stock: $stock")
        apiService.addDHMStock(requireContext(), stock) {
            (activity as MainActivity).hideDialog()
            if (it != null) {
                val dialog = SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success!!")
                    .setContentText(it.message)
                    .setConfirmText("Ok")
                    .setConfirmClickListener { d ->
                        d.dismiss()
                        NavHostFragment.findNavController(this@DhmStockFragment).navigateUp()
                    }
                dialog.setCancelable(false)
                dialog
                    .show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.problems),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }


    private fun observeResourcesSaveAction() {
        viewModel.customMessage.observe(viewLifecycleOwner) {
            if (!it.success) {
                Toast.makeText(
                    requireContext(), it.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
                (activity as MainActivity).hideDialog()
                return@observe
            }

            (activity as MainActivity).hideDialog()
            val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Success")
                .setContentText(resources.getString(R.string.app_okay_saved))
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

    private fun onSubmitAction() {
        pa = binding.edPasteurized.text.toString()
        upa = binding.edUnpasteurized.text.toString()
        prepa = binding.edPrePasteurized.text.toString()
        preupa = binding.edPreUnpasteurized.text.toString()

        if (TextUtils.isEmpty(pa) || pa == "0") {
            binding.edPasteurized.requestFocus()
            binding.tilPasteurized.error = "Enter value"
            return
        }
        if (TextUtils.isEmpty(upa) || upa == "0") {
            binding.edUnpasteurized.requestFocus()
            binding.tilUnpasteurized.error = "Enter value"
            return
        }

        if (TextUtils.isEmpty(prepa) || prepa == "0") {
            binding.edPrePasteurized.requestFocus()
            binding.tilPrePasteurized.error = "Enter value"
            return
        }

        if (TextUtils.isEmpty(preupa) || preupa == "0") {
            binding.edPreUnpasteurized.requestFocus()
            binding.tilPreUnpasteurized.error = "Enter value"
            return
        }

        confirmationDialog.show(childFragmentManager, "Confirm Details")

    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {

        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText(getString(R.string.cancel_questionnaire_message))
            .setConfirmText("Yes")
            .setConfirmClickListener { d ->
                d.dismiss()
                NavHostFragment.findNavController(this@DhmStockFragment).navigateUp()
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