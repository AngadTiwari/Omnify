package com.angad.omnify.callbacks

import com.angad.omnify.models.Comment

/**
 * callback when the comment is fetched via api
 */
interface IOnCommentFetched {
    fun onResponse(comment: Comment?)
}
