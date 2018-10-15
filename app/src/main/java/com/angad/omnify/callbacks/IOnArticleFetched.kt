package com.angad.omnify.callbacks

import com.angad.omnify.models.Article

/**
 * callback when article is fetching using api
 */
interface IOnArticleFetched {
    fun onResponse(i:Int, article: Article?)
}
