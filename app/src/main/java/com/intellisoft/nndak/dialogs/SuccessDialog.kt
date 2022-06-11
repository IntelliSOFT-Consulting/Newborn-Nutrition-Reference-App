package com.intellisoft.nndak.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.intellisoft.nndak.databinding.SuccessDialogBinding
import com.intellisoft.nndak.utils.dimOption

class SuccessDialog(
    private val proceed: () -> Unit,
    private val message: String,
    private val error: Boolean
) : DialogFragment() {
    private var _binding: SuccessDialogBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SuccessDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            title.text = message
            btnSubmit.setOnClickListener { proceed() }
            if (error) {
                tvTitle.text = "Error"
                tvTitle.setTextColor(ColorStateList.valueOf(Color.parseColor("#A8001E")))
                title.setTextColor(ColorStateList.valueOf(Color.parseColor("#A8001E")))
                dimOption(icon, "#A8001E")
            }
        }

    }
}