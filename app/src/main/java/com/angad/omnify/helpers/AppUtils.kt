package com.angad.omnify.helpers

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan

class AppUtils {

    companion object {
        val HACKERNEWS_API_ENDPOINT = "https://hacker-news.firebaseio.com/v0/"
        val MINE_PORTFOLIO_URL: String? = "https://angtwr31.github.io"
        val APP_DATE_FORMAT: String? = "dd MMM yyyy"

        /**
         * bold the text
         * @param str: string to make bold
         * @param start: start index
         * @param end: end index
         */
        fun makeTextBold(str:String, start:Int, end:Int): SpannableStringBuilder {
            val sb = SpannableStringBuilder(str)

            val bss = StyleSpan(Typeface.BOLD)
            sb.setSpan(bss, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            return sb
        }
    }
}