package com.example.shediz.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.shediz.data.network.TaskResult
import com.example.shediz.model.Post
import com.example.shediz.data.network.Resource
import com.example.shediz.repository.PostFullRepository
import io.reactivex.disposables.CompositeDisposable

class FeedViewModel: ViewModel()
{
    companion object
    {
        private val TAG = "TAG_" + FeedViewModel::class.simpleName
    }

    private val disposables = CompositeDisposable()

    val postsLiveData = MutableLiveData<Resource<List<Post>?>>()

    val taskLiveData = MutableLiveData<Resource<TaskResult>>()

    private val postRepository = PostFullRepository(postsLiveData, taskLiveData, disposables)

    fun requestDeletePost(postId: String)
    {
        postRepository.deletePost(postId)
    }

    fun loadFeed(page: Int)
    {
        postRepository.loadFeed(page)
    }

    fun requestLikePost(postId: String)
    {
        postRepository.addLike(postId)
    }

    fun requestUnLikePost(postId: String)
    {
        postRepository.removeLike(postId)
    }

    override fun onCleared()
    {
        Log.i(TAG, "onClear()")
        disposables.dispose()
        super.onCleared()
    }
}