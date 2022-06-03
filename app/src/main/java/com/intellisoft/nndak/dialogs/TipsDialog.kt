package com.intellisoft.nndak.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.intellisoft.nndak.databinding.TipsDialogBinding
import com.intellisoft.nndak.models.FeedingCuesTips

class TipsDialog(
    private val proceed: (FeedingCuesTips) -> Unit,

    ) : DialogFragment() {
    private var _binding: TipsDialogBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TipsDialogBinding.inflate(inflater, container, false)
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


                val feeding = FeedingCuesTips(
                    readiness = readiness,
                    latch = latch,
                    steady = steady,
                    audible = audible,
                    chocking = chocking,
                )
                proceed(feeding)

            }
        }

    }

}