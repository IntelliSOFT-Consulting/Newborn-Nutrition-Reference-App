package com.intellisoft.nndak.adapters

import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class CustomAdapter(@NonNull fm: FragmentManager?) :
    FragmentPagerAdapter(fm!!) {
    private val mFragments: MutableList<Fragment> = ArrayList()
    private val mStrings: MutableList<String> = ArrayList()

    @NonNull
    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }

    override fun getCount(): Int {
        return mStrings.size
    }

    @Nullable
    override fun getPageTitle(position: Int): CharSequence? {
        return mStrings[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragments.add(fragment)
        mStrings.add(title)
    }
}