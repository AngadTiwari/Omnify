package com.angad.omnify.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.angad.omnify.AppController
import com.angad.omnify.callbacks.IOnArticleFetched
import com.angad.omnify.R
import com.angad.omnify.adapters.ArticlesListAdapter
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.helpers.SharedPreferenceHelper
import com.angad.omnify.models.Article
import com.angad.omnify.models.ArticleEvent
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_articles_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.auth.FirebaseAuth
import io.realm.RealmResults

/**
 * @author Angad Tiwari
 * @msg article list screen
 */
class ArticlesListActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private val tag: String? = ArticlesListAdapter::class.java.simpleName

    private lateinit var articles_adapter: ArticlesListAdapter
    private var articles: MutableList<Article>? = mutableListOf()
    private lateinit var onScrollListener: RecyclerView.OnScrollListener

    private lateinit var layoutManger: LinearLayoutManager
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles_list)

        initRealmDb()
        initView()
        attachListeners()
        fetchArticles()
    }

    /**
     * attach the listeners & handlers
     */
    private fun attachListeners() {
        recycler_repos.addOnScrollListener(onScrollListener)
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    /**
     * init realm database to load data from local device
     */
    private fun initRealmDb() {
        realm = Realm.getDefaultInstance()
    }

    /**
     * on logout auth state change calls
     */
    override fun onAuthStateChanged(auth: FirebaseAuth) {
        if (auth.currentUser == null) {
            this@ArticlesListActivity.finish()
        }
    }

    /**
     * fetch the trending github android repos via github v3 api
     */
    private fun fetchArticles() {
        SharedPreferenceHelper.setSharedPreferenceLong(this, SharedPreferenceHelper.LAST_UPDATED_DATE, System.currentTimeMillis())
        AppController.getService()?.getTopArticles()?.enqueue(object: Callback<ArrayList<Int>> {
            override fun onResponse(call: Call<ArrayList<Int>>?, response: Response<ArrayList<Int>>?) {
                when(response?.code()) {
                    200 -> {
                        response.body()?.let {
                            articles?.clear()
                            response.body()?.forEachIndexed { index, it ->
                                val article = Article()
                                article.id = it
                                articles?.add(article)
                                articles_adapter.notifyItemInserted(index)
                            }
                            refreshArticles(0, 6)
                        }
                    }
                    else -> {
                        Toast.makeText(this@ArticlesListActivity, "Error while fetching android trending repos", Toast.LENGTH_LONG).show()
                    }
                }
                layout_progress.visibility = View.GONE
            }

            override fun onFailure(call: Call<ArrayList<Int>>?, t: Throwable?) {
                Toast.makeText(this@ArticlesListActivity, "Error while fetching android trending repos", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * fetch article using id for index & callback iOnArticleFetched
     */
    private fun fetchArticleFromId(id:Int, index:Int, callback: IOnArticleFetched) {
        AppController.getService()?.getArticleFromId(id)?.enqueue(object: Callback<Article> {
            override fun onResponse(call: Call<Article>?, response: Response<Article>?) {
                callback.onResponse(index, response?.body())
            }

            override fun onFailure(call: Call<Article>?, t: Throwable?) {
                callback.onResponse(index, null)
            }
        })
    }

    /**
     * initializing the views
     */
    private fun initView() {
        setSupportActionBar(toolbar)
        layoutManger = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recycler_repos.layoutManager = layoutManger
        articles_adapter = ArticlesListAdapter(this@ArticlesListActivity, articles)
        recycler_repos.adapter = articles_adapter

        /**
         * scroll state change listener to update last updated time & fetching the articles at runtime
         */
        onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                takeIf { newState==RecyclerView.SCROLL_STATE_SETTLING }.apply {
                    refreshArticles(layoutManger.findFirstVisibleItemPosition(), layoutManger.findLastVisibleItemPosition())
                    refreshUpdatedTime()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        }
    }

    /**
     * check if article already exist on realm then show from there or else call api to fetch article
     */
    private fun refreshArticles(firstVisiblePosition:Int, lastVisiblePosition:Int) {
        Log.d(tag, "fetching articles from $firstVisiblePosition to $lastVisiblePosition")
        var index = firstVisiblePosition
        while (index != lastVisiblePosition+1) {
            realm.executeTransaction {
                val articleExist = realm.where(Article::class.java).equalTo("id", articles?.get(index)?.id).findFirst()
                if (articleExist != null) {
                    Log.d(tag, "realm find the data on index $index")
                    articles?.set(index, articleExist)
                    articles_adapter.notifyItemChanged(index)
                } else {
                    Log.d(tag, "realm won't find the data on index $index")
                    fetchArticleFromId(articles?.get(index)?.id!!, index, object: IOnArticleFetched {
                        override fun onResponse(i: Int, article: Article?) {
                            when (article) {
                                null -> Log.d(tag, "error while fetching article with id:$articles?.get(index)?.id!!")
                                else -> {
                                    articles?.set(i, article)
                                    articles_adapter.notifyItemChanged(i)
                                    realm.executeTransaction {
                                        realm.insertOrUpdate(article)
                                    }
                                }
                            }
                        }
                    })
                }
                index++
            }
        }
    }

    /**
     * update last updated time
     */
    private fun refreshUpdatedTime() {
        val last_updated = SharedPreferenceHelper.getSharedPreferenceLong(this@ArticlesListActivity, SharedPreferenceHelper.LAST_UPDATED_DATE, System.currentTimeMillis())
        var updated_ago = DateUtils.getRelativeTimeSpanString(last_updated, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        if(updated_ago == "0 seconds ago"){
            updated_ago = "just now"
        }
        toolbar.subtitle = String.format(resources.getString(R.string.subtile), updated_ago)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ArticleEvent) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_articles_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.item_portfolio -> {
                val url = AppUtils.MINE_PORTFOLIO_URL
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this) //register to eventbus
    }

    override fun onResume() {
        super.onResume()
        refreshUpdatedTime()
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this) //unregister to eventbus
    }

    /**
     * show dialog for user's confirmation, that this action will signout the user
     */
    override fun onBackPressed() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.logout_title)
        dialogBuilder.setMessage(R.string.logout_message)
        dialogBuilder.setPositiveButton("Do it") { dialogInterface, i ->
            FirebaseAuth.getInstance().signOut()
        }
        dialogBuilder.setNegativeButton("Discard") { dialog, whichButton -> /*pass*/}
        dialogBuilder.setCancelable(false)
        dialogBuilder.create().show()
    }
}
