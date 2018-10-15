package com.angad.omnify.networks

import com.angad.omnify.models.Comment
import com.angad.omnify.models.Article
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * @author Angad Tiwari
 * @msg hacker news service interface to call retrofit api call
 */
interface HackerNewsService {

    @GET("topstories.json")
    fun getTopArticles(): Call<ArrayList<Int>>

    @GET("item/{storyid}.json")
    fun getArticleFromId(@Path("storyid" ) storyid: Int): Call<Article>


    @GET("item/{commentid}.json")
    fun getCommentFromId(@Path("commentid") commentid: Int): Call<Comment>
}