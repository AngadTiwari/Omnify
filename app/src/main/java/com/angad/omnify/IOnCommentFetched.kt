package com.angad.omnify

import com.angad.omnify.models.Comment

interface IOnCommentFetched {
    fun onResponse(comment: Comment?)
}
