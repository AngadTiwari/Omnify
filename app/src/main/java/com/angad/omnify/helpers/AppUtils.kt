package com.angad.omnify.helpers

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan

/**
 * @author Angad Tiwari
 * @msg util class for app omnify
 */
class AppUtils {

    companion object {
        const val HACKERNEWS_API_ENDPOINT = "https://hacker-news.firebaseio.com/v0/"
        const val MINE_PORTFOLIO_URL = "https://angtwr31.github.io"
        const val APP_DATE_FORMAT = "dd MMM yyyy"
        const val TAB_COMMENT = "comment"
        const val TAB_WEBVIEW = "tab_webview"

        const val COMMENTS_IDS_OBJECT = "comments_ids"
        const val ARTICLE_URL_OBJECT = "article_url"

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