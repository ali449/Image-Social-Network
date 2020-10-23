package com.example.shediz.repository

import androidx.lifecycle.MutableLiveData
import com.example.shediz.App
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.Resource
import com.example.shediz.model.Post
import com.example.shediz.utils.Constants
import com.example.shediz.utils.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.Executors

class RecRepository(private val result: MutableLiveData<Resource<List<Post>?>>,
                    private val disposables: CompositeDisposable)
{
    private val dao = App.instance.db.postDao()

    fun recommendMe(page: Int)
    {
        result.value = Resource.loading(null, page)

        Executors.newCachedThreadPool().execute {
            result.postValue(Resource.success(dao.loadLimitedRecPosts(Constants.PAGE_SIZE,
                Constants.PAGE_SIZE * page), page))
        }

        if (!Util.isNetworkAvailable(App.instance))
            return

        ApiService.getRecommendedPosts(page).subscribe(object : SingleObserver<List<String>>
        {
            override fun onSuccess(t: List<String>)
            {
                val mappedData = t.map{Post(it, "", "", Date(), false,
                    null, null, null)}
                result.value = Resource.success(mappedData, page)

                loadPosts(t, page)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                result.value = Resource.error(e)
            }
        })
    }

    private fun loadPosts(pidList: List<String>, page: Int)
    {
        if (pidList.isEmpty())
            return

        result.value = Resource.loading(null, page)

        ApiService.getBatchPosts(pidList).subscribe(object : SingleObserver<List<Post>>
        {
            override fun onSuccess(posts: List<Post>)
            {
                val mappedResult = posts.map {post->
                    post.type = 2
                    post
                }

                result.value = Resource.success(mappedResult, page)

                Executors.newCachedThreadPool().execute {
                    dao.addPosts(mappedResult)
                }
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                result.value = Resource.error(e)
            }
        })
    }
}