package com.angad.omnify.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.angad.omnify.AppController
import com.angad.omnify.IOnCommentFetched
import com.angad.omnify.R
import com.angad.omnify.adapters.CommentsListAdapter
import com.angad.omnify.adapters.DetailPagerAdapter
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.models.Article
import com.angad.omnify.models.ArticleEvent
import com.angad.omnify.models.Comment
import kotlinx.android.synthetic.main.activity_article_detail.*
import kotlinx.android.synthetic.main.fragment_comments.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArticleDetailActivity : AppCompatActivity(), View.OnClickListener, AppBarLayout.OnOffsetChangedListener {

    private val tag: String? = ArticleDetailActivity::class.java.simpleName
    private val comments: MutableList<Comment> = mutableListOf()
    private var adapter_comments: CommentsListAdapter? = null
    private var mArticle: Article? = null

    private lateinit var mDetailPagerAdapter: DetailPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        initView()
        attachListeners()
    }

    private fun attachListeners() {
        fab.setOnClickListener(this)
        app_bar.addOnOffsetChangedListener(this)
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        //recycler_comments.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //adapter_comments = CommentsListAdapter(this@ArticleDetailActivity, comments)
        //recycler_comments.adapter = adapter_comments

        mDetailPagerAdapter = DetailPagerAdapter(supportFragmentManager)
    }

    /**
     * onclick handler
     */
    override fun onClick(view: View?) {
        when(view?.id) {
        //clicking to this floating btn, opens mine portfolio
            R.id.fab -> {
                val url = AppUtils.MINE_PORTFOLIO_URL
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }
    }

    override fun onOffsetChanged(appbar: AppBarLayout?, verticalOffset: Int) {
        if (Math.abs(verticalOffset)-appbar?.getTotalScrollRange()!! == 0) {
            toolbar_layout.title = mArticle?.title
        } else {
            toolbar_layout.title = ""
        }
    }

    /**
     * eventbus handler to fetch the repository data from repo-list screen
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ArticleEvent) {
        Log.d(tag, event.toString())

        mArticle = event?.article
        bindData(event?.article)

        AppController.getService()?.getArticleFromId(event.article.id)?.enqueue(object: Callback<Article> {
            override fun onResponse(call: Call<Article>?, response: Response<Article>?) {
                bindData(response?.body())
                when(response?.code()) {
                    200 -> {
                        mArticle = response.body()
                        response.body()?.kids?.let {
                            comments?.clear()
                            response.body()?.kids?.forEach {
                                comments?.add(Comment())
                            }
                            response.body()?.kids?.forEachIndexed { index, it ->
                                fetchCommentFromId(it, object: IOnCommentFetched {
                                    override fun onResponse(comment: Comment?) {
                                        when(comment) {
                                            null -> Log.d(tag, "error while fetching comment with id:$it")
                                            else -> {
                                                comments?.set(index, comment)
                                                //adapter_comments?.notifyItemChanged(index)
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    }
                    else -> {
                        Toast.makeText(this@ArticleDetailActivity, "Error while fetching android trending repos", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<Article>?, t: Throwable?) {
                Log.d(tag, "failure while fetching update article data")
            }
        })
    }

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

    private fun bindData(article: Article?) {
        txt_article_title.text = article?.title
        txt_article_url.text = article?.url
        txt_article_by.text = AppUtils.makeTextBold(String.format(resources.getString(R.string.label_by), article?.by.toString()), 0, 3)
        txt_article_time.text = AppUtils.makeTextBold(String.format(resources.getString(R.string.label_time), DateUtils.getRelativeTimeSpanString(article?.time?.toLong()!!*1000, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)), 0, 8)

        createTabsOrNot()
    }

    private fun createTabsOrNot() {
        if(mArticle?.url.isNullOrEmpty())
            return
        // Create a tab listener that is called when the user changes tabs.
        val tabListener: android.app.ActionBar.TabListener = object: android.app.ActionBar.TabListener{
            override fun onTabReselected(p0: android.app.ActionBar.Tab?, p1: android.app.FragmentTransaction?) {

            }

            override fun onTabSelected(p0: android.app.ActionBar.Tab?, p1: android.app.FragmentTransaction?) {

            }

            override fun onTabUnselected(p0: android.app.ActionBar.Tab?, p1: android.app.FragmentTransaction?) {

            }
        }
        tabs.removeAllTabs()
        tabs.addTab(tabs.newTab().setText(mArticle?.kids?.size.toString()+" COMMENTS"))
        tabs.addTab(tabs.newTab().setText("ARTICLES"))
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this) //register to eventbus
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this) //unregister to eventbus
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            //On back btn click, close the screen back to repo-list screen
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
