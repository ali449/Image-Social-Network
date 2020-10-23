package com.example.shediz.data.network

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.shediz.App
import com.example.shediz.R
import com.example.shediz.model.Comment
import com.example.shediz.model.Like
import com.example.shediz.model.Post
import com.example.shediz.model.User
import com.example.shediz.utils.Constants.BASE_URL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


object ApiService
{
    private const val AUTHORIZATION = "Authorization"

    var token = "Bearer " + App.instance.prefs.getUserToken()

    fun requestFollow(userName: String): Single<TaskResult>
    {
        return Rx2AndroidNetworking.put("$BASE_URL/request_follow/{username}")
            .addHeaders(AUTHORIZATION, token)
            .addPathParameter("username", userName)
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .map { TaskResult(it.success, userName, null, null, TaskType.USER_FOLLOW) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun requestUnFollow(userName: String): Single<TaskResult>
    {
        return Rx2AndroidNetworking.delete("$BASE_URL/request_unfollow/{username}")
            .addHeaders(AUTHORIZATION, token)
            .addPathParameter("username", userName)
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .map { TaskResult(it.success, userName, null, null, TaskType.USER_UNFOLLOW) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun notifyFollowResult(userName: String, hasAccept: Boolean): Single<TaskResult>
    {
        return Rx2AndroidNetworking.put("$BASE_URL/request_follow_result/{username}")
            .addHeaders(AUTHORIZATION, token)
            .addPathParameter("username", userName)
            .addQueryParameter("has_accept", hasAccept.toString())
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .map { TaskResult(it.success, userName, null, null, TaskType.USER_FOLLOW_REQUEST) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createPost(file: File, content: String): Single<CreateResponse> =
        ImageUploader(AUTHORIZATION, token).uploadPost(file, content)

    fun deletePost(postId: String): Single<TaskResult>
    {
        return Rx2AndroidNetworking.delete("$BASE_URL/post/{id}")
            .addHeaders(AUTHORIZATION, token)
            .addPathParameter("id", postId)
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .map { TaskResult(it.success, null, postId, null, TaskType.POST_REMOVED) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadProfileImage(context: Context, userName: String): RequestBuilder<Drawable>
    {
        return Glide.with(context)
            .load("$BASE_URL/s/pic_profile/$userName")
            .placeholder(R.drawable.placeholder_user)
            .error(R.drawable.placeholder_user)
            .fallback(R.drawable.placeholder_user)
    }

    fun loadThumbPostImage(context: Context, postId: String): RequestBuilder<Drawable>
    {
        return Glide
            .with(context)
            .load(GlideUrl("$BASE_URL/post/thumb_image/$postId", getImageAuthHeader()))
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.placeholder_loading)
            .error(R.drawable.placholder_error)
            .fallback(R.drawable.placeholder_fallback)
    }

    fun loadFullPostImage(context: Context, postId: String): RequestBuilder<Drawable>
    {
        return Glide
            .with(context)
            .load(GlideUrl("$BASE_URL/post/main_image/$postId", getImageAuthHeader()))
            .thumbnail(Glide.with(context).load("$BASE_URL/post/s/thumb_image/$postId"))
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.placeholder_loading)
            .error(R.drawable.placholder_error)
            .fallback(R.drawable.placeholder_fallback)
    }

    private fun getImageAuthHeader(): LazyHeaders = LazyHeaders.Builder()
        .addHeader(AUTHORIZATION, token).build()

    fun createComment(postId: String, text: String): Single<TaskResult>
    {
        val jsonObject = JSONObject()
        jsonObject.put("text", text)

        return Rx2AndroidNetworking.post("$BASE_URL/score/add_comment")
            .addHeaders(AUTHORIZATION, token)
            .addQueryParameter("pid", postId)
            .addJSONObjectBody(jsonObject)
            .build()
            .getObjectSingle(CommentPutResponse::class.java)
            .map { TaskResult(true, null, postId, it.cid, TaskType.COMMENT_CREATED) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun removeComment(commentId: Long, postId: String): Single<TaskResult>
    {
        return Rx2AndroidNetworking.delete("$BASE_URL/score/rm_comment")
            .addHeaders(AUTHORIZATION, token)
            .addQueryParameter("cid", commentId.toString())
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .map { TaskResult(it.success, null, postId, commentId, TaskType.COMMENT_REMOVED) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createLike(postId: String): Single<TaskResult>
    {
        return Rx2AndroidNetworking.post("$BASE_URL/score/add_like")
            .addHeaders(AUTHORIZATION, token)
            .addQueryParameter("pid", postId)
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .map { TaskResult(it.success, null, postId, null, TaskType.POST_LIKED) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun removeLike(postId: String): Single<TaskResult>
    {
        return Rx2AndroidNetworking.delete("$BASE_URL/score/rm_like")
            .addHeaders(AUTHORIZATION, token)
            .addQueryParameter("pid", postId)
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .map { TaskResult(it.success, postId, null, null, TaskType.POST_UNLIKED) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getLikesByPostIds(pidList: List<String>): Single<List<Like>>
    {
        val loggedInUserName = App.instance.prefs.getUserName()

        val jsonObject = JSONObject()
        jsonObject.put("username", loggedInUserName)
        jsonObject.put("ids",  JSONArray(pidList))

        return Rx2AndroidNetworking.put("$BASE_URL/score/s/likes_pid_list")
            .addJSONObjectBody(jsonObject)
            .build()
            .getObjectListSingle(Like::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getComments(postId: String, page: Int): Single<List<Comment>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/score/comments")
            .addHeaders(AUTHORIZATION, token)
            .addQueryParameter("pid", postId)
            .addQueryParameter("page", page.toString())
            .build()
            .getObjectListSingle(Comment::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getFollowingUserNames(userName: String, page: Int): Single<List<String>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/following/{username}")
            .addHeaders(AUTHORIZATION, token)
            .addPathParameter("username", userName)
            .addQueryParameter("page", page.toString())
            .build()
            .getObjectListSingle(String::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getFollowersUserNames(userName: String, page: Int): Single<List<String>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/followers/{username}")
            .addHeaders(AUTHORIZATION, token)
            .addPathParameter("username", userName)
            .addQueryParameter("page", page.toString())
            .build()
            .getObjectListSingle(String::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUserWithFollowStatus(userName: String): Single<UserWithFollow>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/user_with_follow/{targetUserName}")
            .addHeaders(AUTHORIZATION, token)
            .addPathParameter("targetUserName", userName)
            .build()
            .getObjectSingle(UserWithFollow::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUser(userName: String): Single<User>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/s/user/{username}")
            .addPathParameter("username", userName)
            .addQueryParameter("needFullInfo", false.toString())
            .build()
            .getObjectSingle(User::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun searchUser(query: String): Single<List<User>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/s/search_user")
            .addQueryParameter("q", query)
            .build()
            .getObjectListSingle(User::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun searchInAllPosts(query: String, page: Int): Single<List<Post>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/post/s/search_post")
            .addQueryParameter("q", query)
            .addQueryParameter("page", page.toString())
            .build()
            .getObjectListSingle(Post::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateVisitedTag(tag: String, countVisitedPosts: Int): Single<String>
    {
        val jsonObject = JSONObject()
        jsonObject.put("tag", tag)
        jsonObject.put("count", countVisitedPosts)

        return Rx2AndroidNetworking.put("$BASE_URL/recommender/searched_tag")
            .addHeaders(AUTHORIZATION, token)
            .addJSONObjectBody(jsonObject)
            .build()
            .getObjectSingle(String::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteVisitedTags(): Single<String>
    {
        return Rx2AndroidNetworking.delete("$BASE_URL/recommender/rm_searched_tag_all")
            .addHeaders(AUTHORIZATION, token)
            .build()
            .getObjectSingle(String::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun searchByTag(tag: String, page: Int): Single<List<Post>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/post/s/tags/{tag}")
            .addPathParameter("tag", tag)
            .addQueryParameter("page", page.toString())
            .build()
            .getObjectListSingle(Post::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getBatchPosts(pidList: List<String>): Single<List<Post>>
    {
        val jsonObject = JSONObject()
        jsonObject.put("ids",  JSONArray(pidList))

        return Rx2AndroidNetworking.put("$BASE_URL/post/s/ids")
            .addJSONObjectBody(jsonObject)
            .build()
            .getObjectListSingle(Post::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getSuggestedTags(tag: String): Single<List<String>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/post/s/suggest_tags")
            .addQueryParameter("t", tag)
            .addQueryParameter("page", 0.toString())
            .build()
            .getObjectListSingle(String::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUserPosts(userName: String, page: Int): Single<List<Post>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/post/user/{username}")
            .addPathParameter("username", userName)
            .addQueryParameter("page", page.toString())
            .addHeaders(AUTHORIZATION, token)
            .build()
            .getObjectListSingle(Post::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getRecommendedPosts(page: Int): Single<List<String>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/recommender")
            .addHeaders(AUTHORIZATION, token)
            .addQueryParameter("page", page.toString())
            .build()
            .stringSingle
            .map {
                val response = it.substring(1, it.length-1)
                val myType = object : TypeToken<List<String>>() {}.type
                Gson().fromJson<List<String>>(response, myType)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNewPosts(page: Int): Single<List<Post>>
    {
        return Rx2AndroidNetworking.get("$BASE_URL/post/new_posts")
            .addHeaders(AUTHORIZATION, token)
            .addQueryParameter("page", page.toString())
            .build()
            .getObjectListSingle(Post::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun editUser(bio: String, isPrivate: Boolean): Single<TaskResponse>
    {
        return Rx2AndroidNetworking.put("$BASE_URL/edit_user")
            .addHeaders(AUTHORIZATION, token)
            .addQueryParameter("bio", bio)
            .addQueryParameter("is_private", isPrivate.toString())
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun uploadProfilePic(file: File): Single<TaskResponse>
    {
        return Rx2AndroidNetworking.upload("$BASE_URL/up_pic_profile")
            .addMultipartFile("file", file)
            .addHeaders(AUTHORIZATION, token)
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun register(userName: String, passWord: String, bio: String, isPrivate: Boolean): Single<TaskResponse>
    {
        return Rx2AndroidNetworking.post("$BASE_URL/register")
            .addQueryParameter("username", userName)
            .addQueryParameter("password", passWord)
            .addQueryParameter("bio", bio)
            .addQueryParameter("is_private", isPrivate.toString())
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun login(userName: String, passWord: String): Single<LoginResponse>
    {
        return Rx2AndroidNetworking.post("$BASE_URL/login")
            .addQueryParameter("username", userName)
            .addQueryParameter("password", passWord)
            .build()
            .getObjectSingle(LoginResponse::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun logout(): Single<TaskResponse>
    {
        return Rx2AndroidNetworking.delete("$BASE_URL/logout")
            .addHeaders(AUTHORIZATION, token)
            .build()
            .getObjectSingle(TaskResponse::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
