package com.angad.omnify.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.angad.omnify.R
import com.angad.omnify.activities.ArticleDetailActivity
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.models.Article
import com.angad.omnify.models.ArticleEvent
import com.angad.omnify.models.Comment
import kotlinx.android.synthetic.main.adapter_comment_list.view.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class CommentsListAdapter(val context: Context, val comments: MutableList<Comment>?): RecyclerView.Adapter<CommentsListAdapter.ArticlesListAdapterViewHolder>() {

    private val formatToShow: SimpleDateFormat = SimpleDateFormat(AppUtils.APP_DATE_FORMAT, Locale.ENGLISH)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ArticlesListAdapterViewHolder {
        return ArticlesListAdapterViewHolder(this, LayoutInflater.from(context).inflate(R.layout.adapter_comment_list, parent, false))
    }

    /**
     * bind the data to the view
     */
    override fun onBindViewHolder(holder: ArticlesListAdapterViewHolder, position: Int) {
        val comment: Comment? = comments?.get(position)

        holder.txt_comment.text = Html.fromHtml(comment?.text)
        holder.txt_by.text = comment?.by
        holder.txt_time.text = DateUtils.getRelativeTimeSpanString(comment?.time?.toLong()!!*1000, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
    }

    override fun getItemCount(): Int {
        if(comments == null)
            return 0
        return comments.size
    }

    /**
     * adapter's viewholder to initialize views & bind the listeners
     */
    class ArticlesListAdapterViewHolder(adapter: CommentsListAdapter, view: View): RecyclerView.ViewHolder(view) {
        val txt_comment = view.txt_comment
        val txt_by = view.txt_by
        val txt_time = view.txt_time
    }
}