package com.example.shediz.repository

import androidx.lifecycle.*
import com.example.shediz.App
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.TaskResult
import com.example.shediz.model.Like
import com.example.shediz.model.Post
import com.example.shediz.data.network.Resource
import com.example.shediz.utils.Constants.PAGE_SIZE
import com.example.shediz.utils.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.Executors

class PostFullRepository(private val result: MutableLiveData<Resource<List<Post>?>>,
                         private val resultTask: MutableLiveData<Resource<TaskResult>>,
                         private val disposables: CompositeDisposable)
{
    private val dao = App.instance.db.postDao()

    fun deletePost(postId: String)
    {
        resultTask.value = Resource.loading()

        ApiService.deletePost(postId).subscribe(object : SingleObserver<TaskResult>
        {
            override fun onSuccess(t: TaskResult)
            {
                resultTask.value = Resource.success(t)

                Executors.newCachedThreadPool().execute {
                    dao.deletePost(postId)
                }
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultTask.value = Resource.error(e, null)
            }
        })
    }

    fun addLike(postId: String)
    {
        resultTask.value = Resource.loading(null)

        ApiService.createLike(postId).subscribe(object : SingleObserver<TaskResult>
        {
            override fun onSuccess(t: TaskResult)
            {
                resultTask.value = Resource.success(t)

                if (t.success)
                    Executors.newCachedThreadPool().execute {
                        dao.updateLikeStatus(t.pid!!, true)
                    }
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultTask.value = Resource.error(e, null)
            }
        })
    }

    fun removeLike(postId: String)
    {
        resultTask.value = Resource.loading(null)

        ApiService.removeLike(postId).subscribe(object : SingleObserver<TaskResult>
        {
            override fun onSuccess(t: TaskResult)
            {
                resultTask.value = Resource.success(t)

                if (t.success)
                    Executors.newCachedThreadPool().execute {
                        dao.updateLikeStatus(t.pid!!, false)
                    }
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultTask.value = Resource.error(e, null)
            }
        })
    }

    fun loadFeed(page: Int)
    {
        result.value = Resource.loading(null, page)

        Executors.newCachedThreadPool().execute {
            result.postValue(Resource.success(dao.loadLimitedFeed(PAGE_SIZE, PAGE_SIZE * page), page))
        }

        if (!Util.isNetworkAvailable(App.instance))
            return

        ApiService.getNewPosts(page).subscribe(object : SingleObserver<List<Post>>
        {
            override fun onSuccess(response: List<Post>)
            {
                result.value = Resource.success(response, page)

                loadLikes(response, page)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                result.value = Resource.error(e, null)
            }
        })
    }

    private fun loadLikes(response: List<Post>, page: Int)
    {
        if (response.isEmpty())
            return

        val postIdList = response.map { it.id }
        ApiService.getLikesByPostIds(postIdList).subscribe(object : SingleObserver<List<Like>>
        {
            override fun onSuccess(likes: List<Like>)
            {
                val likesByPid = likes.associateBy { it.postId }

                val postFull = response.map {post->
                    post.type = 0
                    likesByPid[post.id]?.let { like ->
                        post.numLikes = like.numLikes
                        post.isUserLiked = like.isUserLiked
                    }
                    post
                }

                result.value = Resource.success(postFull, page)

                Executors.newCachedThreadPool().execute {
                    dao.addPosts(postFull)
                }
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                result.value = Resource.error(e, null)
            }
        })
    }
}