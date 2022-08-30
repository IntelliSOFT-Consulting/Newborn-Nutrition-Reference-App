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
import com.intellisoft.nndak.models.BreastsHistory
import com.intellisoft.nndak.models.PositioningHistory
import com.intellisoft.nndak.utils.boldText

class ViewPositioning(
    private val data: PositioningHistory
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
            tvHeader.text = getString(R.string.pos_history)
            firstLayer.tvhDate.text = getString(R.string._date)
            firstLayer.tvhFrequency.text = getString(R.string.app_cleaned)
            firstLayer.tvhTiming.text = getString(R.string.mother_position)

            layerOne.tvhDate.text = data.date
            layerOne.tvhFrequency.text = data.hands
            layerOne.tvhTiming.text = data.mum

            secondLayer.tvhDate.text = getString(R.string.baby_position)
            secondLayer.tvhFrequency.text = getString(R.string.attachment_data)
            secondLayer.tvhTiming.text = getString(R.string.suckle_data)

            layerTwo.tvhDate.text = data.baby
            layerTwo.tvhFrequency.text = data.attach
            layerTwo.tvhTiming.text = data.suckle

            thirdLayer.lnParent.visibility = View.VISIBLE
            thirdLayer.tvhDate.text = getString(R.string.sucking)
            thirdLayer.tvhFrequency.visibility = View.GONE
            thirdLayer.tvhTiming.visibility = View.GONE

            layerThree.lnParent.visibility = View.VISIBLE
            layerThree.tvhDate.text = data.suckle

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

class ViewBreastFeeding(
    private val data: BreastsHistory
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
            tvHeader.text = getString(R.string.tv_breast_history)
            firstLayer.tvhDate.text = getString(R.string._date)
            firstLayer.tvhFrequency.text = getString(R.string._interest)
            firstLayer.tvhTiming.text = getString(R.string._cues)

            layerOne.tvhDate.text = data.date
            layerOne.tvhFrequency.text = data.interest
            layerOne.tvhTiming.text = data.cues

            secondLayer.tvhDate.text = getString(R.string._sleep)
            secondLayer.tvhFrequency.text = getString(R.string._burst)
            secondLayer.tvhTiming.text = getString(R.string._short_feed)

            layerTwo.tvhDate.text = data.sleep
            layerTwo.tvhFrequency.text = data.bursts
            layerTwo.tvhTiming.text = data.shortFeed

            thirdLayer.tvhDate.text = getString(R.string._rhythmical)
            thirdLayer.tvhFrequency.text = getString(R.string._skin_color)
            thirdLayer.tvhTiming.text = getString(R.string._nipples)

            layerThree.tvhDate.text = data.longSwallow
            layerThree.tvhFrequency.text = data.skin
            layerThree.tvhTiming.text = data.nipples

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