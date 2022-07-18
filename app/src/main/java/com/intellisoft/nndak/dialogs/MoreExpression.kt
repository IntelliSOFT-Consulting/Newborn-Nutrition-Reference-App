package com.intellisoft.nndak.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.DialogExpressionSuccessBinding
import com.intellisoft.nndak.models.ExpressionHistory
import com.intellisoft.nndak.utils.boldText


class MoreExpression(
    private val data: ExpressionHistory
) : DialogFragment() {
    private var _binding: DialogExpressionSuccessBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogExpressionSuccessBinding.inflate(inflater, container, false)
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

            firstLayer.tvhDate.text = getString(R.string._date)
            firstLayer.tvhFrequency.text = getString(R.string.exp_freq)
            firstLayer.tvhTiming.text = getString(R.string.timings)

            layerOne.tvhDate.text = data.date
            layerOne.tvhFrequency.text = data.frequency
            layerOne.tvhTiming.text = data.timing

            secondLayer.tvhDate.text = getString(R.string.stimulating)
            secondLayer.tvhFrequency.text = getString(R.string.hand_expres)
            secondLayer.tvhTiming.text = getString(R.string.breast_pump)

            layerTwo.tvhDate.text = data.massage
            layerTwo.tvhFrequency.text = data.handExpression
            layerTwo.tvhTiming.text = data.breastCondition

            thirdLayer.tvhDate.text = getString(R.string.breast_cond)
            thirdLayer.tvhFrequency.text = getString(R.string.milk_flow)
            thirdLayer.tvhTiming.text = getString(R.string.milk_volume)

            layerThree.tvhDate.text = data.breastCondition
            layerThree.tvhFrequency.text = data.milkVolume
            layerThree.tvhTiming.text = data.milkVolume

            boldText(firstLayer.tvhDate)
            boldText(firstLayer.tvhFrequency)
            boldText(firstLayer.tvhTiming)

            boldText(secondLayer.tvhDate)
            boldText(secondLayer.tvhFrequency)
            boldText(secondLayer.tvhTiming)

            boldText(thirdLayer.tvhDate)
            boldText(thirdLayer.tvhFrequency)
            boldText(thirdLayer.tvhTiming)

            hideView(firstLayer.tvhView)
            hideView(secondLayer.tvhView)
            hideView(thirdLayer.tvhView)
            hideView(layerOne.tvhView)
            hideView(layerTwo.tvhView)
            hideView(layerThree.tvhView)
              tvCancel.setOnClickListener {
                  dialog?.dismiss()
              }
        }

    }

    private fun hideView(tvhView: TextView) {
        tvhView.visibility = View.GONE
    }

}