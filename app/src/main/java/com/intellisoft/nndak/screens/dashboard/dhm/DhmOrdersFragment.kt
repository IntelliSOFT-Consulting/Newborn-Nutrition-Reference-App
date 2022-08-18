package com.intellisoft.nndak.screens.dashboard.dhm

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.State
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.OrdersAdapter
import com.intellisoft.nndak.charts.ItemOrder
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.FragmentDhmOrdersBinding
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.logic.Logics.Companion.ADMINISTRATOR
import com.intellisoft.nndak.logic.Logics.Companion.DOCTOR
import com.intellisoft.nndak.logic.Logics.Companion.HMB_ASSISTANT
import com.intellisoft.nndak.screens.dashboard.RegistrationFragment
import com.intellisoft.nndak.utils.boldText
import com.intellisoft.nndak.viewmodels.MainActivityViewModel
import com.intellisoft.nndak.viewmodels.PatientListViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


class DhmOrdersFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var searchView: SearchView
    private var _binding: FragmentDhmOrdersBinding? = null
    private val apiService = RestManager()
    private val binding
        get() = _binding!!

    private lateinit var mySpinner: Spinner
    private val orderList = ArrayList<ItemOrder>()
    lateinit var adapterList: OrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDhmOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_dhm_orders)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        try {

            val recyclerView: RecyclerView = binding.patientList
            adapterList = OrdersAdapter(orderList, this::onOrderClick)
            recyclerView.adapter = adapterList
            adapterList.submitList(orderList)
            adapterList.notifyDataSetChanged()

            recyclerView.addItemDecoration(

                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                    setDrawable(ColorDrawable(Color.LTGRAY))
                }
            )
            binding.apply {
                breadcrumb.page.text =
                    Html.fromHtml("HMB > <font color=\"#37379B\">Orders</font>")
                breadcrumb.page.setOnClickListener {
                    findNavController().navigateUp()
                }
                incTitle.lnParent.visibility = View.GONE
                swipeRefresh.setOnRefreshListener {
                    loadOrders()
                }
            }
            loadOrders()


        } catch (e: Exception) {
            e.printStackTrace()
        }

        mySpinner = binding.mySpinner

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = adapter
        mySpinner.onItemSelectedListener = this

        searchView = binding.search
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    adapterList.filter.filter(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    adapterList.filter.filter(query)
                    return true
                }
            }
        )
        searchView.setOnQueryTextFocusChangeListener { view, focused ->
            if (!focused) {
                // hide soft keyboard
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (searchView.query.isNotEmpty()) {
                            searchView.setQuery("", true)
                        } else {
                            isEnabled = false
                            activity?.onBackPressed()
                        }
                    }
                }
            )

        binding.apply {
            boldText(binding.incTitle.appMotherName)
            boldText(binding.incTitle.appIpNumber)
            boldText(binding.incTitle.appBabyName)
            boldText(binding.incTitle.appBabyAge)
            boldText(binding.incTitle.appDhmType)
            boldText(binding.incTitle.appConsent)
            boldText(binding.incTitle.appAction)
            actionAddRecipient.setOnClickListener {
                val bundle =
                    bundleOf(RegistrationFragment.QUESTIONNAIRE_FILE_PATH_KEY to "client-registration.json")
                findNavController().navigate(
                    DhmOrdersFragmentDirections.navigateToAddRecipient(),

                    )
            }

        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

    }

    private fun loadOrders() {
        orderList.clear()
        binding.swipeRefresh.isRefreshing = true
        apiService.loadOrders(requireContext()) {
            try {
                binding.apply {
                    if (it != null) {
                        if (it.data.isEmpty()) {
                            empty.cpBgView.visibility = View.VISIBLE
                            swipeRefresh.isRefreshing = false
                        }
                        if (it.data.isNotEmpty()) {

                            empty.cpBgView.visibility = View.GONE
                            swipeRefresh.isRefreshing = false
                            incTitle.lnParent.visibility = View.VISIBLE
                            orderList.clear()
                            it.data.forEach { order ->
                                if (order.motherName != "null") {
                                    orderList.add(order)
                                }
                            }
                            adapterList.notifyDataSetChanged()
                        }
                    } else {
                        empty.cpBgView.visibility = View.VISIBLE
                        swipeRefresh.isRefreshing = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            R.id.menu_profile -> {
                (requireActivity() as MainActivity).navigate(R.id.profileFragment)
                return true
            }
            else -> false
        }
    }

    private fun onOrderClick(it: ItemOrder) {
        val role = (requireActivity() as MainActivity).retrieveUser(true)
        if (role.isNotEmpty()) {
            if (role == ADMINISTRATOR || role == DOCTOR || role == HMB_ASSISTANT) {

                try {
                    val gson = Gson()
                    val json = gson.toJson(it)
                    FhirApplication.updateCurrentOrder(requireContext(), json)

                    findNavController().navigate(
                        DhmOrdersFragmentDirections.navigateToProcessing(),
                    )
                } catch (e: Exception) {

                }
            } else {
                accessDenied()
            }

        } else {
            Toast.makeText(requireContext(), "Please try again", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onResume() {
        try {
            loadOrders()
        } catch (e: Exception) {

        }
        super.onResume()
    }

    private fun accessDenied() {
        SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
            .setTitleText("Access Denied!!")
            .setContentText("You are not Authorized")
            .setCustomImage(R.drawable.smile)

            .show()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val text: String = p0?.getItemAtPosition(p2).toString()
        filterRecords(text)


    }

    private fun filterRecords(text: String) {
        when (text) {
            "Sort By:" -> {
                adapterList.filter.filter("")
            }
            "Term" -> {
                adapterList.filter.filter("Term")
            }
            "Signed" -> {
                adapterList.filter.filter("Signed")
            }
            "Unsigned" -> {
                adapterList.filter.filter("Not Signed")
            }
            "Preterm" -> {
                adapterList.filter.filter("Preterm")
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}