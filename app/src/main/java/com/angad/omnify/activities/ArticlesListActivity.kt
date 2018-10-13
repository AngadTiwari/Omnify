package com.angad.omnify.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.angad.omnify.AppController
import com.angad.omnify.IOnArticleFetched
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

class ArticlesListActivity : AppCompatActivity() {

    private val tag: String? = ArticlesListAdapter::class.java.simpleName

    private var articles_adapter: ArticlesListAdapter? = null
    private var articles: MutableList<Article>? = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles_list)

        initRealmDb()
        initView()
        fetchArticles()
    }

    private fun initRealmDb() {
        val config: RealmConfiguration = RealmConfiguration.Builder().build()
        val realm: Realm = Realm.getInstance(config) // Get a Realm instance for this thread
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
                            response.body()?.forEach {
                                articles?.add(Article())
                            }
                            response.body()?.forEachIndexed { index, it ->
                                fetchArticleFromId(it, object: IOnArticleFetched {
                                    override fun onResponse(article: Article?) {
                                        when(article) {
                                            null -> Log.d(tag, "error while fetching article with id:$it")
                                            else -> {
                                                articles?.set(index, article)
                                                articles_adapter?.notifyItemChanged(index)
                                            }
                                        }
                                    }
                                })
                            }
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

    private fun fetchArticleFromId(id:Int, callback: IOnArticleFetched) {
        AppController.getService()?.getArticleFromId(id)?.enqueue(object: Callback<Article> {
            override fun onResponse(call: Call<Article>?, response: Response<Article>?) {
                callback.onResponse(response?.body())
            }

            override fun onFailure(call: Call<Article>?, t: Throwable?) {
                callback.onResponse(null)
            }
        })
    }

    /**
     * initializing the views
     */
    private fun initView() {
        recycler_repos.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        articles_adapter = ArticlesListAdapter(this@ArticlesListActivity, articles)
        recycler_repos.adapter = articles_adapter
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ArticleEvent) {

    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this) //register to eventbus
    }

    override fun onResume() {
        super.onResume()

        val last_updated = SharedPreferenceHelper.getSharedPreferenceLong(this, SharedPreferenceHelper.LAST_UPDATED_DATE, System.currentTimeMillis())
        var updated_ago = DateUtils.getRelativeTimeSpanString(last_updated, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        if(updated_ago == "0 minutes ago"){
            updated_ago = "just now"
        }
        toolbar.subtitle = String.format(resources.getString(R.string.subtile), updated_ago)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this) //unregister to eventbus
    }

    override fun onBackPressed() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.logout_title)
        dialogBuilder.setMessage(R.string.logout_message)
        dialogBuilder.setPositiveButton("Do it", {dialogInterface, i ->  this@ArticlesListActivity.finish() })
        dialogBuilder.setNegativeButton("Discard", { dialog, whichButton -> /*pass*/})
        dialogBuilder.setCancelable(false)
        dialogBuilder.create().show()
    }
}
