package com.angad.omnify.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.angad.omnify.fragments.CommentsFragment
import com.angad.omnify.fragments.WebviewFragment
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.models.Article
import com.angad.omnify.models.PagerData

/**
 * detail page view pager's adapter to bind the proper fragment to tabs (comment or articlewebview)
 */
class DetailPagerAdapter(val fm: FragmentManager, val pagerData: ArrayList<PagerData>) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int = pagerData.size

    override fun getItem(i: Int): Fragment? {
        when(pagerData.get(i)?.type){
            AppUtils.TAB_COMMENT -> {
                val fragment = CommentsFragment()
                fragment.arguments = Bundle().apply {
                    putIntegerArrayList(AppUtils.COMMENTS_IDS_OBJECT, ArrayList(pagerData.get(i)?.data?.kids))
                }
                return fragment
            }
            AppUtils.TAB_WEBVIEW -> {
                val fragment = WebviewFragment()
                fragment.arguments = Bundle().apply {
                    putString(AppUtils.ARTICLE_URL_OBJECT, pagerData.get(i)?.data?.url)
                }
                return fragment
            }
        }
        return null
    }

    override fun getPageTitle(position: Int): CharSequence {
        return "OBJECT " + (position + 1)
    }
}