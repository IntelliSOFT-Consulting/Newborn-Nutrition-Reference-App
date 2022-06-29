package com.intellisoft.nndak.screens.profile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.auth.LoginActivity
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.databinding.FragmentProfileBinding
import com.intellisoft.nndak.viewmodels.MainActivityViewModel
import timber.log.Timber


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val viewModel: MainActivityViewModel by viewModels()
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_profile)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)
        observeLastSyncTime()
        loadAccountDetails()

        binding.apply {
            lytLogout.setOnClickListener {
                confirmLogout()
            }
        }


    }

    private fun observeLastSyncTime() {
        try {
            val time = FhirApplication.getSyncTime(requireContext())
            binding.apply {
                tvSync.text = time
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun confirmLogout() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            FhirApplication.setLoggedIn(requireContext(), false)
            requireActivity().finishAffinity()
            val i = Intent(requireContext(), LoginActivity::class.java)
            startActivity(i)
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun loadAccountDetails() {
        val user = FhirApplication.getProfile(requireContext())
        if (user != null) {
            val gson = Gson()
            try {
                val it: User = gson.fromJson(user, User::class.java)
                binding.apply {
                    tvName.text = it.names
                    tvEmail.text = it.email
                    tvRole.text = it.role
                    tvSince.text = it.createdAt.substring(0, 10)
                }

            } catch (e: Exception) {

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