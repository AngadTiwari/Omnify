package com.angad.omnify.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.angad.omnify.AppController
import com.angad.omnify.callbacks.IOnCommentFetched
import com.angad.omnify.R
import com.angad.omnify.adapters.CommentsListAdapter
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.models.Comment
import kotlinx.android.synthetic.main.fragment_comments.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * comment fragment inside detail page
 */
class CommentsFragment: Fragment() {

    private var comments_id: ArrayList<Int> = arrayListOf()
    private val comments: MutableList<Comment> = mutableListOf()
    private var adapter_comments: CommentsListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View = inflater.inflate(R.layout.fragment_comments, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey(AppUtils.COMMENTS_IDS_OBJECT) }?.apply {
            bindData()
            fetchComments()
        }
    }

    /**
     * bind tthe data to comments recylcer view
     */
    private fun bindData() {
        comments_id = ArrayList(arguments?.getIntegerArrayList(AppUtils.COMMENTS_IDS_OBJECT))
        recycler_comments.layoutManager = LinearLayoutManager(this@CommentsFragment.context!!, RecyclerView.VERTICAL, false)
        adapter_comments = CommentsListAdapter(this@CommentsFragment.context!!, comments)
        recycler_comments.adapter = adapter_comments
    }

    /**
     * fetch the latest comment
     */
    private fun fetchComments() {
        comments_id.forEach {
            comments.add(Comment())
        }
        comments_id.forEachIndexed { index, it ->
            fetchCommentFromId(it, object: IOnCommentFetched {
                override fun onResponse(comment: Comment?) {
                    when(comment) {
                        null -> Log.d(tag, "error while fetching comment with id:$it")
                        else -> {
                            comments?.set(index, comment)
                            adapter_comments?.notifyItemChanged(index)
                        }
                    }
                }
            })
        }
    }

    /**
     * fetch each comment via id
     */
    fun fetchCommentFromId(id: Int, callback: IOnCommentFetched) {
        AppController.getService()?.getCommentFromId(id)?.enqueue(object: Callback<Comment> {
            override fun onResponse(call: Call<Comment>?, response: Response<Comment>?) {
                callback.onResponse(response?.body())
            }

            override fun onFailure(call: Call<Comment>?, t: Throwable?) {
                callback.onResponse(null)
            }
        })
    }
}