package com.intellisoft.nndak.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.intellisoft.nndak.databinding.FeedingCuesDialogBinding
import com.intellisoft.nndak.models.FeedingCuesTips

class FeedingCuesDialog(
    private val proceed: (FeedingCuesTips) -> Unit,

    ) : DialogFragment() {
    private var _binding: FeedingCuesDialogBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FeedingCuesDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lnCancel.setOnClickListener {
                dialog?.dismiss()
            }
            btnSubmit.setOnClickListener {
                val readiness = if (rbYesReadiness.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val latch = if (rbYesLatch.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val steady = if (rbYesSteady.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val audible = if (rbYesAudible.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val chocking = if (rbYesChocking.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val softening = if (rbYesSoftening.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val tenSide = if (rbYesSideFeed.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val threeHours = if (rbYesThreeHours.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val sixDiapers = if (rbYesDiapers.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                val contra = if (rbYesContra.isChecked) {
                    "Yes"
                } else {
                    "No"
                }

                val feeding = FeedingCuesTips(
                    readiness = readiness,
                    latch = latch,
                    steady = steady,
                    audible = audible,
                    chocking = chocking,
                    softening = softening,
                    tenSide = tenSide,
                    threeHours = threeHours,
                    sixDiapers = sixDiapers,
                    contra = contra
                )
                proceed(feeding)

            }
        }

    }

}