package com.angad.omnify.activities

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.angad.omnify.AppController
import com.angad.omnify.R
import com.angad.omnify.adapters.DetailPagerAdapter
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.models.Article
import com.angad.omnify.models.ArticleEvent
import com.angad.omnify.models.PagerData
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_article_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author Angad Tiwari
 * @msg Article Detail page containing 2 child fragment using viewpager
 */
class ArticleDetailActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener, TabLayout.OnTabSelectedListener {

    private val tag: String? = ArticleDetailActivity::class.java.simpleName
    private var mArticle: Article? = null

    private lateinit var mDetailPagerAdapter: DetailPagerAdapter
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        initView()
        attachListeners()
    }

    /**
     * attach the listeners and handlers
     */
    private fun attachListeners() {
        app_bar.addOnOffsetChangedListener(this)
    }

    /**
     * init views
     */
    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    /**
     * on collapsing toolbar offset change listener to show/hide toolbar with title
     */
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
        updateTabs()

        /**
         * fetching the artcle using id
         */
        AppController.getService()?.getArticleFromId(event.article.id)?.enqueue(object: Callback<Article> {
            override fun onResponse(call: Call<Article>?, response: Response<Article>?) {
                bindData(response?.body())
                when(response?.code()) {
                    200 -> {
                        mArticle = response.body()
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

    /**
     * bind data to view
     */
    private fun bindData(article: Article?) {
        txt_article_title.text = article?.title
        txt_article_url.text = article?.url
        txt_article_by.text = AppUtils.makeTextBold(String.format(resources.getString(R.string.label_by), article?.by.toString()), 0, 3)
        txt_article_time.text = AppUtils.makeTextBold(String.format(resources.getString(R.string.label_time), DateUtils.getRelativeTimeSpanString(article?.time?.toLong()!!*1000, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)), 0, 8)
    }

    /**
     * update tab, if no web url then don;t create article tab and if zero comment then don't create comments tab
     */
    private fun updateTabs() {
        tabs.removeAllTabs()
        var pagerData = arrayListOf<PagerData>()
        if(mArticle?.kids != null && mArticle?.kids?.isNotEmpty()!!){
            tabs.addTab(tabs.newTab().setText(mArticle?.kids?.size.toString()+" COMMENTS"))
            val article: Article? = mArticle
            pagerData.add(PagerData(AppUtils.TAB_COMMENT, article!!))
        }
        if(!mArticle?.url?.isNullOrBlank()!!){
            tabs.addTab(tabs.newTab().setText("ARTICLES"))
            val article: Article? = mArticle
            pagerData.add(PagerData(AppUtils.TAB_WEBVIEW, article!!))
        }
        tabs.addOnTabSelectedListener(this)

        mDetailPagerAdapter = DetailPagerAdapter(supportFragmentManager, pagerData)
        pager_comments_webview.adapter = mDetailPagerAdapter
    }

    /**
     * on tab select change, change the viewpager current page
     */
    override fun onTabSelected(p0: TabLayout.Tab?) {
        pager_comments_webview.currentItem = p0?.position!!
    }

    override fun onTabReselected(p0: TabLayout.Tab?) {

    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {

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
