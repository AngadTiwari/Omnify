package com.angad.omnify.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.angad.omnify.R
import com.angad.omnify.activities.ArticleDetailActivity
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.models.Article
import com.angad.omnify.models.ArticleEvent
import kotlinx.android.synthetic.main.adapter_articles_list.view.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Angad Tiwari
 * @msg articles list adapter for each row
 */
class ArticlesListAdapter(val context: Context, val articles: MutableList<Article>?): RecyclerView.Adapter<ArticlesListAdapter.ArticlesListAdapterViewHolder>() {

    private val formatToShow: SimpleDateFormat = SimpleDateFormat(AppUtils.APP_DATE_FORMAT, Locale.ENGLISH)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ArticlesListAdapterViewHolder {
        return ArticlesListAdapterViewHolder(this, LayoutInflater.from(context).inflate(R.layout.adapter_articles_list, parent, false))
    }

    /**
     * bind the data to the view
     */
    override fun onBindViewHolder(holder: ArticlesListAdapterViewHolder, position: Int) {
        val article: Article? = articles?.get(position)

        if(article?.title.isNullOrBlank()) {
            holder.txt_loading.visibility = View.VISIBLE
            return
        }

        holder.txt_loading.visibility = View.GONE
        holder.txt_score.text = article?.score.let {
            if(it==null) {
                return@let "0"
            }
            return@let article?.score.toString()
        }
        holder.txt_title.text = article?.title
        holder.txt_url.text = article?.url
        holder.txt_time.text = DateUtils.getRelativeTimeSpanString(article?.time?.toLong()!!*1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        holder.txt_usr.text = "- ${article?.by}"
        holder.txt_comments.text = article?.kids.let {
            if(it==null) {
                return@let "0"
            }
            return@let article?.kids?.size.toString()
        }
    }

    override fun getItemCount(): Int {
        if(articles == null)
            return 0
        return articles.size
    }

    /**
     * adapter's viewholder to initialize views & bind the listeners
     */
    class ArticlesListAdapterViewHolder(adapter: ArticlesListAdapter, view: View): RecyclerView.ViewHolder(view) {
        val card_article = view.card_article
        val txt_score = view.txt_score
        val txt_comments = view.txt_comments
        val txt_title = view.txt_title
        val txt_url = view.txt_article_url
        val txt_time = view.txt_time
        val txt_usr = view.txt_usr
        val txt_loading = view.txt_loading

        init {
            card_article.setOnClickListener(View.OnClickListener {
                val intent: Intent = Intent(adapter.context, ArticleDetailActivity::class.java)
                adapter.context.startActivity(intent)
                //sending the repo data with a-bit delay, while the detail screen will register to eventbus
                card_article.postDelayed(Runnable {
                    EventBus.getDefault().post(ArticleEvent(article = adapter.articles?.get(adapterPosition)!!))
                }, 1200)
            })
        }
    }
}