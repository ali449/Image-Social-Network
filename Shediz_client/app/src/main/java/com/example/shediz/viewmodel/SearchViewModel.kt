package com.example.shediz.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shediz.data.network.Resource
import com.example.shediz.model.Post
import com.example.shediz.model.User
import com.example.shediz.repository.SearchRepository
import io.reactivex.disposables.CompositeDisposable

class SearchViewModel : ViewModel()
{
    companion object
    {
        private val TAG = "TAG_" + SearchViewModel::class.simpleName
    }

    private val disposables = CompositeDisposable()

    val postsLiveData = MutableLiveData<Resource<List<Post>?>>()

    val strLiveData = MutableLiveData<Resource<List<String>?>>()

    val userLiveData = MutableLiveData<Resource<List<User>?>>()

    private val repository = SearchRepository(postsLiveData, strLiveData, userLiveData, disposables)

    fun updateVisitedTag(tag: String, countVisitedPosts: Int)
    {
        repository.updateVisitedTag(tag, countVisitedPosts)
    }

    fun searchUser(query: String)
    {
        repository.searchUser(query)
    }

    fun searchInAllPosts(query: String, page: Int)
    {
        repository.searchInPosts(query, page)
    }

    fun searchTag(tag: String, page: Int)
    {
        repository.searchByTag(tag, page)
    }

    fun suggestTags(tag: String)
    {
        repository.suggestTags(tag)
    }

    override fun onCleared()
    {
        Log.i(TAG, "onClear()")
        disposables.dispose()
        super.onCleared()
    }
}