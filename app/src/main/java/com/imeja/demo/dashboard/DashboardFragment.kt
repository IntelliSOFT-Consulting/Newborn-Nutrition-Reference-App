package com.imeja.demo.dashboard

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.imeja.demo.MainActivity
import com.imeja.demo.R
import com.imeja.demo.databinding.FragmentDashboardBinding
import com.imeja.demo.databinding.PatientDetailBinding
import com.imeja.demo.utils.ImageUtils
import com.imeja.demo.utils.ThemeUtils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var mBitmapCache: SparseArray<Bitmap?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentDashboardBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
        }

        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

        binding.rltMaternity.setOnClickListener {
            findNavController().navigate(DashboardFragmentDirections.actionDashboardToPatientList("0"))
        }
        binding.newBornButton.setOnClickListener {
            findNavController().navigate(DashboardFragmentDirections.actionDashboardToPatientList("1"))
        }
        binding.newbornPatientsButton.setOnClickListener {
            findNavController().navigate(DashboardFragmentDirections.actionDashboardToPatientList("2"))
        }
        binding.postNatalButton.setOnClickListener {
            findNavController().navigate(DashboardFragmentDirections.actionDashboardToPatientList("3"))
        }
        binding.allPatientsButton.setOnClickListener {
            findNavController().navigate(DashboardFragmentDirections.actionDashboardToPatientList("4"))
        }
        binding.humanMilkButton.setOnClickListener {
            findNavController().navigate(DashboardFragmentDirections.actionDashboardToPatientList("5"))
        }
        binding.monitoringButton.setOnClickListener {
            findNavController().navigate(DashboardFragmentDirections.actionDashboardToPatientList("6"))
        }


    }

    private fun drawIcons() {
        drawIcon(binding.maternityButton, R.drawable.ic_baseline_circle)
        drawIcon(binding.newBornButton, R.drawable.ic_baseline_circle)
        drawIcon(binding.allPatientsButton, R.drawable.ic_baseline_circle)
        drawIcon(binding.humanMilkButton, R.drawable.ic_baseline_circle)

    }

    private fun drawIcon(imageView: ImageView?, drawableId: Int) {
        mBitmapCache = SparseArray()
        if (view != null) {
            createImageBitmap(drawableId, imageView!!.layoutParams)
            imageView.setImageBitmap(mBitmapCache!![drawableId])
        }
    }

    private fun createImageBitmap(key: Int, layoutParams: ViewGroup.LayoutParams) {
        if (mBitmapCache!![key] == null) {
            mBitmapCache!!.put(key, ImageUtils.decodeBitmapFromResource(resources, key,
                layoutParams.width, layoutParams.height))
        }
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}