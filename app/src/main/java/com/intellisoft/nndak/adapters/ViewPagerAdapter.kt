package com.intellisoft.nndak.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.intellisoft.nndak.R

class ViewPagerAdapter(private val context: Context) : PagerAdapter() {

    private val images = intArrayOf(
        R.drawable.behavior,
        R.drawable.pediatrics,
        R.drawable.healthcare,
        R.drawable.checkup
    )

    private val headings = intArrayOf(
        R.string.heading_one,
        R.string.heading_two,
        R.string.heading_three,
        R.string.heading_fourth
    )

    private val description = intArrayOf(
        R.string.desc_one,
        R.string.desc_two,
        R.string.desc_three,
        R.string.desc_fourth
    )


    override fun getCount(): Int {
        return headings.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(R.layout.slider_layout, container, false)
        val slidetitleimage = view.findViewById<ImageView>(R.id.titleImage)
        val slideHeading = view.findViewById<TextView>(R.id.texttitle)
        val slideDesciption = view.findViewById<TextView>(R.id.textdeccription)

        slidetitleimage.setImageResource(images[position])
        slideHeading.setText(headings[position])
        slideDesciption.setText(description[position])
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}