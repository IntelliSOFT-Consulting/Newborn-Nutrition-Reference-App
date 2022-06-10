package com.intellisoft.nndak.screens.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentLandingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LandingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LandingFragment : Fragment() {
    private var _binding: FragmentLandingBinding? = null
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.home_menu_dashboard)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        binding.apply {
            cntBaby.setOnClickListener {
                findNavController().navigate(LandingFragmentDirections.navigateToBabies())

            }
            cntRegister.setOnClickListener {
                findNavController().navigate(LandingFragmentDirections.navigateToRegistration())
            }
            cntMilk.setOnClickListener {
                findNavController().navigate(LandingFragmentDirections.navigateToMilk())
            }
            cntStatistics.setOnClickListener {
                findNavController().navigate(LandingFragmentDirections.navigateToStatistics())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}