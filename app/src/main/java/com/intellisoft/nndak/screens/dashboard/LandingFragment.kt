package com.intellisoft.nndak.screens.dashboard

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.LandingAdapter
import com.intellisoft.nndak.databinding.FragmentLandingBinding
import com.intellisoft.nndak.viewmodels.LayoutListViewModel

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
    private val viewModel: LayoutListViewModel by viewModels()
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

//        setUpLayoutsRecyclerView()
        binding.apply {
            cntBaby.setOnClickListener {
                findNavController().navigate(LandingFragmentDirections.navigateToBabies())

            }
            cntRegister.setOnClickListener {
                findNavController().navigate(LandingFragmentDirections.navigateToRegistration())
            }
            cntMilk.setOnClickListener {
                val allowed = (activity as MainActivity).dhmAllowed()
                if (allowed) {
                    findNavController().navigate(LandingFragmentDirections.navigateToMilk())
                } else {
                    (activity as MainActivity).accessDenied()
                }
            }
            cntStatistics.setOnClickListener {
                val allowed = (activity as MainActivity).statisticsAllowed()
                if (allowed) {
                    findNavController().navigate(LandingFragmentDirections.navigateToStatistics())
                } else {
                    (activity as MainActivity).accessDenied()
                }
            }
        }
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
            R.id.menu_notification -> {
                (requireActivity() as MainActivity).navigate(R.id.notificationFragment)
                return true
            }
            else -> false
        }
    }

    /* private fun setUpLayoutsRecyclerView() {
         val adapter =
             LandingAdapter(::onItemClick).apply { submitList(viewModel.getLayoutList()) }
         val recyclerView = requireView().findViewById<RecyclerView>(R.id.sdcLayoutsRecyclerView)
         recyclerView.adapter = adapter
         recyclerView.layoutManager = GridLayoutManager(context, 2)
     }*/

    /*  private fun onItemClick(layout: LayoutListViewModel.Layout) {
          // TODO Remove check when all layout questionnaire json are updated.
          // https://github.com/google/android-fhir/issues/1079
          if (layout.questionnaireFileName.isEmpty()) {
              return
          }
          launchQuestionnaireFragment(layout)
      }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}