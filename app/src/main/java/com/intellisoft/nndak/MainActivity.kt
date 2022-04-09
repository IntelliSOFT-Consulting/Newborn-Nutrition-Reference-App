package com.intellisoft.nndak

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.sync.State
import com.intellisoft.nndak.auth.LoginActivity
import com.intellisoft.nndak.dashboard.DashboardFragment
import com.intellisoft.nndak.databinding.ActivityMainBinding
import com.intellisoft.nndak.viewmodels.MainActivityViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val MAX_RESOURCE_COUNT = 20
const val CURRENT_ORGANIZATION ="NAIROBI"// "Pumwani Maternity Hospital"
const val USER_ADDRESS = "NAIROBI"
const val USER_COUNTRY = "KE"
const val SYNC_PARAM="address-city"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val TAG = javaClass.name
    private val viewModel: MainActivityViewModel by viewModels()

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
        cacheDir.deleteRecursively()
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
        binding.drawer.setDrawerLockMode(lockMode)
        drawerToggle.isDrawerIndicatorEnabled = enabled
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

    private fun initNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected)
        drawerToggle = ActionBarDrawerToggle(this, binding.drawer, R.string.open, R.string.close)
        binding.drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
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
                    FhirApplication.setLoggedIn(this,false)
                    finishAffinity()
                    val i = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(i)

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

    private fun replaceFragment(fragment: DashboardFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }

    private fun showToast(message: String) {
        Log.i(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeSyncState() {
        lifecycleScope.launch {
            viewModel.pollState.collect {
                Log.d(TAG, "observerSyncState: pollState Got status $it")
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
        viewModel.lastSyncTimestampLiveData.observe(
            this
        ) {
            binding.navigationView.getHeaderView(0)
                .findViewById<TextView>(R.id.last_sync_tv).text = it
        }
    }
}
