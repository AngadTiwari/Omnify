package com.angad.omnify.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.angad.omnify.R

private const val COMMENTS_IDS_OBJECT = "comments_ids"

class CommentsFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // The last two arguments ensure LayoutParams are inflated properly.
        val rootView: View = inflater.inflate(R.layout.fragment_comments, container, false)
        arguments?.takeIf { it.containsKey(COMMENTS_IDS_OBJECT) }?.apply {
            //TODO
        }
        return rootView
    }
}