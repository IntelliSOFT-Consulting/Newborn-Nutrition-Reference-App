package com.intellisoft.nndak

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.fhir.sync.State
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.intellisoft.nndak.auth.LoginActivity
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.ActivityMainBinding
import com.intellisoft.nndak.dialogs.CustomProgressDialog
import com.intellisoft.nndak.screens.dashboard.RegistrationFragment
import com.intellisoft.nndak.viewmodels.MainActivityViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private val progressDialog = CustomProgressDialog()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val TAG = javaClass.name
    private val viewModel: MainActivityViewModel by viewModels()
    private var exit = false

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

    private fun handleMenuClicks() {

        val navController = findNavController(R.id.nav_host_fragment)
        binding.apply {
            menu.lnHome.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                navController.navigate(R.id.homeFragment)
            }

            menu.lnStatistics.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                navController.navigate(R.id.statisticsFragment)
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
                navController.navigate(R.id.dhmOrdersFragment)
            }
            menu.lnDhmStock.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                navController.navigateUp()
                val bundle =
                    bundleOf(RegistrationFragment.QUESTIONNAIRE_FILE_PATH_KEY to "dhm-stock.json")
                findNavController(R.id.nav_host_fragment).navigate(
                    R.id.dhmStockFragment,
                    bundle
                )

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
                navController.navigate(R.id.homeFragment)
            }
            menu.lnSync.setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                viewModel.poll()
            }
        }
    }

    fun displayDialog() {

        progressDialog.show(this@MainActivity, "Please wait...")
    }

    fun hideDialog() {
        progressDialog.dialog.dismiss()
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


    @RequiresApi(Build.VERSION_CODES.O)
    fun openNavigationDrawer() {
        binding.drawer.openDrawer(GravityCompat.START)
        viewModel.updateLastSyncTimestamp()
    }

    private fun initActionBar() {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun initNavigationDrawer() {
        val drawerLayout: DrawerLayout = binding.drawer
        val navView: BottomNavigationView = binding.navigation
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.landingFragment, R.id.babiesFragment, R.id.registrationFragment,
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        binding.navigation.visibility = View.VISIBLE
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

    private fun openRegistration() {
        val bundle =
            bundleOf(RegistrationFragment.QUESTIONNAIRE_FILE_PATH_KEY to "client-registration.json")
        findNavController(R.id.nav_host_fragment).navigate(
            R.id.registrationFragment,
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
        apiService.loadUser(this) {

            if (it != null) {
                val gson = Gson()
                val json = gson.toJson(it.data)
                FhirApplication.updateProfile(this, json)
            } else {
                Timber.e("Error")
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeSyncState() {
        lifecycleScope.launch {
            viewModel.pollState.collect {
                Timber.d("observerSyncState: pollState Got status $it")
                when (it) {
                    is State.Started -> showToast("Sync: started")
                    is State.InProgress -> showToast("Sync: in progress with ${it.resourceType?.name}")
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
        /*  viewModel.lastSyncTimestampLiveData.observe(
              this
          ) {
              binding.navigationView.getHeaderView(0)
                  .findViewById<TextView>(R.id.last_sync_tv).text = it
          }*/
    }

    fun navigate(resource: Int) {
        findNavController(R.id.nav_host_fragment).navigate(resource)
    }
}
