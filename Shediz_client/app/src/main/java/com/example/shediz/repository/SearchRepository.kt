package com.example.shediz.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.androidnetworking.error.ANError
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.Resource
import com.example.shediz.model.Post
import com.example.shediz.model.User
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class SearchRepository(private val resultPost: MutableLiveData<Resource<List<Post>?>>,
                       private val resultTagSuggest: MutableLiveData<Resource<List<String>?>>,
                       private val resultUser: MutableLiveData<Resource<List<User>?>>,
                       private val disposables: CompositeDisposable)
{
    fun updateVisitedTag(tag: String, countVisitedPosts: Int)
    {
        ApiService.updateVisitedTag(tag, countVisitedPosts).subscribe(object : SingleObserver<String>
        {
            override fun onSuccess(t: String)
            {
                Log.i("TAG_SearchRepository", t)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                if (e is ANError)
                    Log.e("TAG_SearchRepository",
                        "Error: ${e.errorCode}, Body: ${e.errorBody}, Details: ${e.errorDetail}")
            }
        })
    }

    fun searchUser(query: String)
    {
        resultUser.value = Resource.loading()

        ApiService.searchUser(query).subscribe(object : SingleObserver<List<User>>
        {
            override fun onSuccess(t: List<User>)
            {
                resultUser.value = Resource.success(t)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultUser.value = Resource.error(e)
            }
        })
    }

    fun searchInPosts(query: String, page: Int)
    {
        resultPost.value = Resource.loading()

        ApiService.searchInAllPosts(query, page).subscribe(object : SingleObserver<List<Post>>
        {
            override fun onSuccess(t: List<Post>)
            {
                resultPost.value = Resource.success(t, page)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultPost.value = Resource.error(e)
            }
        })
    }

    fun searchByTag(tag: String, page: Int)
    {
        resultPost.value = Resource.loading()

        ApiService.searchByTag(tag, page).subscribe(object : SingleObserver<List<Post>>
        {
            override fun onSuccess(t: List<Post>)
            {
                resultPost.value = Resource.success(t, page)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultPost.value = Resource.error(e)
            }
        })
    }

    fun suggestTags(t: String)
    {
        resultTagSuggest.value = Resource.loading()

        ApiService.getSuggestedTags(t).subscribe(object : SingleObserver<List<String>>
        {
            override fun onSuccess(t: List<String>)
            {
                resultTagSuggest.value = Resource.success(t)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultTagSuggest.value = Resource.error(e)
            }
        })
    }
}