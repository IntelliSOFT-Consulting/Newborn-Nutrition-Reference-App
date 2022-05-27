package com.intellisoft.nndak.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.intellisoft.nndak.databinding.ConfirmationDialogBinding

class ConfirmationDialog(
    private val proceed: () -> Unit,
    private val message: String
) : DialogFragment() {
    private var _binding: ConfirmationDialogBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //   dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner)
        _binding = ConfirmationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
          val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
          val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
          dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvTitle.text = message
            btnSubmit.setOnClickListener { proceed() }
        }

    }

}