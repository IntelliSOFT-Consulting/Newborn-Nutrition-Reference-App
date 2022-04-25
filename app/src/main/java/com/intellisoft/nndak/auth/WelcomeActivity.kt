package com.intellisoft.nndak.auth

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.ViewPagerAdapter
import com.intellisoft.nndak.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private var mSLideViewPager: ViewPager? = null
    private var mDotLayout: LinearLayout? = null
    private var backbtn: Button? = null
    private var nextbtn: Button? = null
    private var skipbtn: Button? = null

    lateinit var dots: Array<TextView?>
    private var viewPagerAdapter: ViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        backbtn = binding.backbtn
        nextbtn = binding.nextbtn
        skipbtn = binding.skipButton

        backbtn!!.setOnClickListener {
            if (getItem(0) > 0) {
                mSLideViewPager!!.setCurrentItem(getItem(-1), true)
            }
        }

        nextbtn!!.setOnClickListener {
            if (getItem(0) < 3) mSLideViewPager!!.setCurrentItem(getItem(1), true) else {
                FhirApplication.setWelcomed(this, true)
                val i = Intent(this@WelcomeActivity, SetupActivity::class.java)
                startActivity(i)
                finish()
            }
        }

        skipbtn!!.setOnClickListener {
            FhirApplication.setWelcomed(this, true)
            val i = Intent(this@WelcomeActivity, SetupActivity::class.java)
            startActivity(i)
            finish()
        }

        mSLideViewPager = binding.slideViewPager
        mDotLayout = binding.indicatorLayout

        viewPagerAdapter = ViewPagerAdapter(this)

        mSLideViewPager!!.adapter = viewPagerAdapter

        setUpIndicator(0)
        mSLideViewPager!!.addOnPageChangeListener(viewListener)
    }


    fun setUpIndicator(position: Int) {
        dots = arrayOfNulls(4)
        mDotLayout!!.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = Html.fromHtml("&#8226")
            dots[i]!!.textSize = 35f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dots[i]!!.setTextColor(
                    resources.getColor(
                        R.color.inactive,
                        applicationContext.theme
                    )
                )
            }
            mDotLayout!!.addView(dots[i])
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dots[position]!!.setTextColor(
                resources.getColor(
                    R.color.active,
                    applicationContext.theme
                )
            )
        }
    }

    var viewListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            setUpIndicator(position)
            if (position > 0) {
                backbtn!!.visibility = View.VISIBLE
            } else {
                backbtn!!.visibility = View.INVISIBLE
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    private fun getItem(i: Int): Int {
        return mSLideViewPager!!.currentItem + i
    }
}