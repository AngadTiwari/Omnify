package com.angad.omnify

import com.angad.omnify.models.Article

interface IOnArticleFetched {
    fun onResponse(article: Article?)
}
