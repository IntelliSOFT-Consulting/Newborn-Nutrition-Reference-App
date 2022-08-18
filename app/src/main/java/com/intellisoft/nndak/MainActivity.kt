package com.intellisoft.nndak

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.sync.State
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.intellisoft.nndak.auth.LoginActivity
import com.intellisoft.nndak.auth.PinLockActivity
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.databinding.ActivityMainBinding
import com.intellisoft.nndak.databinding.BabyItemBinding
import com.intellisoft.nndak.dialogs.CustomProgressDialog
import com.intellisoft.nndak.logic.Logics.Companion.ADMINISTRATOR
import com.intellisoft.nndak.logic.Logics.Companion.DEPUTY_NURSE_MANAGER
import com.intellisoft.nndak.logic.Logics.Companion.DOCTOR
import com.intellisoft.nndak.logic.Logics.Companion.HEAD_OF_DEPARTMENT
import com.intellisoft.nndak.logic.Logics.Companion.HMB_ASSISTANT
import com.intellisoft.nndak.logic.Logics.Companion.NURSING_OFFICER_IN_CHARGE
import com.intellisoft.nndak.models.MotherBabyItem
import com.intellisoft.nndak.screens.dashboard.RegistrationFragment
import com.intellisoft.nndak.utils.isNetworkAvailable
import com.intellisoft.nndak.viewmodels.MainActivityViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private val progressDialog = CustomProgressDialog()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val viewModel: MainActivityViewModel by viewModels()
    private var exit = false

    private val gson = Gson()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initActionBar()
        initNavigationDrawer()
        observeLastSyncTime()
        observeSyncState()
        viewModel.updateLastSyncTimestamp()
        handleMenuClicks()


    }


    fun showBottomNavigationView(value: Int) {
        findViewById<BottomNavigationView>(R.id.navigation).visibility = value

    }

    fun dhmAllowed(): Boolean {

        val role = retrieveUser(true)
        if (role.isNotEmpty()) {
            return role == ADMINISTRATOR || role == DOCTOR || role == HMB_ASSISTANT
        }
        return false
    }

    fun statisticsAllowed(): Boolean {

        val role = retrieveUser(true)
        if (role.isNotEmpty()) {
            return role == ADMINISTRATOR
                    || role == HEAD_OF_DEPARTMENT
                    || role == DEPUTY_NURSE_MANAGER || role == NURSING_OFFICER_IN_CHARGE
        }
        return false
    }

    fun retrieveUser(isRole: Boolean): String {

        val user = FhirApplication.getProfile(this)
        val name = try {
            val it: User = gson.fromJson(user, User::class.java)
            if (isRole) {
                it.role
            } else {
                it.names
            }
        } catch (e: Exception) {
            "Unknown"
        }
        return name
    }

    private fun handleMenuClicks() {

        val navController = findNavController(R.id.nav_host_fragment)
        binding.apply {

            menu.lnHome.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                navController.navigate(R.id.landingFragment)
            }

            menu.lnStatistics.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                if (statisticsAllowed()) {
                    navController.navigate(R.id.statisticsFragment)
                } else {
                    accessDenied()
                }
            }

            menu.lnRegister.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                openRegistration()
            }
            menu.lnBabyProfile.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                navController.navigate(R.id.babiesFragment)
            }
            menu.lnDhmOrders.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                if (dhmAllowed()) {
                    navController.navigate(R.id.dhmOrdersFragment)
                } else {
                    accessDenied()
                }
            }
            menu.lnDhmStock.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                if (dhmAllowed()) {
                    val bundle =
                        bundleOf(RegistrationFragment.QUESTIONNAIRE_FILE_PATH_KEY to "dhm-stock.json")
                    findNavController(R.id.nav_host_fragment).navigate(
                        R.id.dhmStockFragment,
                        bundle
                    )
                } else {
                    accessDenied()
                }

            }
            menu.btnCloseFilter.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
            }
            menu.lnProfile.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                navController.navigate(R.id.profileFragment)
            }
            menu.lnSettings.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                navController.navigate(R.id.settingsFragment)
            }
            menu.lnDhmDashboard.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                if (dhmAllowed()) {
                    navController.navigate(R.id.homeFragment)
                } else {
                    accessDenied()
                }
            }
            menu.lnSync.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                Timber.e("Sync Start")
                viewModel.poll()
            }
        }
    }

    fun accessDenied() {
        SweetAlertDialog(this@MainActivity, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
            .setTitleText("Access Denied!!")
            .setContentText("You are not Authorized")
            .setCustomImage(R.drawable.smile)
            .show()
    }

    fun displayDialog() {

        progressDialog.show(this@MainActivity, "Please wait...")
    }

    fun hideDialog() {
        if (progressDialog.dialog.isShowing) {
            progressDialog.dialog.dismiss()
        }
    }

    override fun onBackPressed() {

        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)

            return
        }
        super.onBackPressed()

    }

    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode =
            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        try {
            binding.drawer.setDrawerLockMode(lockMode)
            drawerToggle.isDrawerIndicatorEnabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openDashboard(patientId: String) {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigate(R.id.childDashboardFragment, bundleOf("patient_id" to patientId))
    }

    fun openNavigationDrawer() {
        binding.drawer.openDrawer(GravityCompat.START)
        viewModel.updateLastSyncTimestamp()
    }

    private fun initActionBar() {
        val toolbar = binding.appBarMain.toolbar
        setSupportActionBar(toolbar)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun initNavigationDrawer() {
        val drawerLayout: DrawerLayout = binding.drawer
        val navView: BottomNavigationView = binding.appBarMain.content.navigation
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.landingFragment, R.id.babiesFragment, R.id.registrationFragment,
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        binding.appBarMain.content.navigation.visibility = View.VISIBLE
        binding.navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected)
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        binding.drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        syncProfile()
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.landingFragment -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.landingFragment)
                }
                R.id.babiesFragment -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.babiesFragment)
                }
                R.id.registrationFragment -> {
                    openRegistration()
                }
            }
            false
        }

    fun openRegistration() {
        val bundle =
            bundleOf(RegistrationFragment.QUESTIONNAIRE_FILE_PATH_KEY to "client-registration.json")
        findNavController(R.id.nav_host_fragment).navigate(
            R.id.customRegistrationFragment,
            bundle
        )
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sync -> {
                viewModel.poll()
                true
            }

            R.id.menu_exit -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Logout")
                builder.setMessage("Are you sure you want to logout?")

                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    try {
                        FhirApplication.setLoggedIn(this, false)
                        finishAffinity()
                        val i = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(i)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()
                true
            }

        }
        binding.drawer.closeDrawer(GravityCompat.START)
        return false
    }

    private fun syncProfile() {
        val apiService = RestManager()
        if (isNetworkAvailable(this)) {
            apiService.loadUser(this) {
                if (it != null) {
                    val gson = Gson()
                    val json = gson.toJson(it.data)
                    FhirApplication.updateProfile(this, json)
                } else {
                    // sessionTimeOut()
                }
            }
        }
    }

    fun sessionTimeOut() {
        finishAffinity()
        val i = Intent(this@MainActivity, PinLockActivity::class.java)
        startActivity(i)
        this@MainActivity.overridePendingTransition(
            R.anim.anim_slide_in_left,
            R.anim.anim_slide_out_left
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun observeSyncState() {
        lifecycleScope.launch {
            viewModel.pollState.collect {
                Timber.d("observerSyncState: pollState Got status $it")
                when (it) {
                    is State.Started -> showToast("Sync: started")
                    is State.InProgress -> showToast(
                        "Sync: in progress with ${it.resourceType?.name}"
                    )
                    is State.Finished -> {
                        showToast("Sync: succeeded at ${it.result.timestamp}")
                        viewModel.updateLastSyncTimestamp()
                    }
                    is State.Failed -> {
                        showToast("Sync: failed at ${it.result.timestamp}")
                        viewModel.updateLastSyncTimestamp()
                    }
                    else -> showToast("Sync: unknown state.")
                }
            }
        }
    }

    private fun observeLastSyncTime() {
        viewModel.lastSyncTimestampLiveData.observe(
            this
        ) {
            FhirApplication.updateSyncTime(this@MainActivity, it)
        }
    }

    fun navigate(resource: Int) {
        findNavController(R.id.nav_host_fragment).navigate(resource)
    }

    companion object {

        fun updateBabyMum(incDetails: BabyItemBinding, data: MotherBabyItem) {
            try {
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

                incDetails.appBirthDate.text = data.dashboard.dateOfBirth ?: ""
                incDetails.appLifeDay.text = data.dashboard.dayOfLife ?: ""
                incDetails.appAdmDate.text = data.dashboard.dateOfAdm ?: ""

                incDetails.appNeonatalSepsis.text = data.dashboard.neonatalSepsis ?: ""
                incDetails.appJaundice.text = data.dashboard.jaundice ?: ""
                incDetails.appAsphyxia.text = data.dashboard.asphyxia ?: ""


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
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}
