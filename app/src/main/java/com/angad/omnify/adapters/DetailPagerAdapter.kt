package com.angad.omnify.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.angad.omnify.fragments.CommentsFragment

private const val COMMENTS_IDS_OBJECT = "comments_ids"

class DetailPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int = 100

    override fun getItem(i: Int): Fragment {
        val fragment = CommentsFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(COMMENTS_IDS_OBJECT, i + 1)
        }
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence {
        return "OBJECT " + (position + 1)
    }
}