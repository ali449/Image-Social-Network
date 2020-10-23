package com.example.shediz.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.Resource
import com.example.shediz.repository.SinglePostRepository
import io.reactivex.disposables.CompositeDisposable

class SinglePostViewModel : ViewModel()
{
    companion object
    {
        private val TAG = "TAG_" + SinglePostViewModel::class.simpleName
    }

    private val disposables = CompositeDisposable()

    val taskLiveData = MutableLiveData<Resource<TaskResult>>()

    private val repository = SinglePostRepository(taskLiveData, disposables)

    fun requestDeletePost(postId: String)
    {
        repository.deletePost(postId)
    }

    fun requestLikePost(postId: String)
    {
        repository.addLike(postId)
    }

    fun requestUnLikePost(postId: String)
    {
        repository.removeLike(postId)
    }

    override fun onCleared()
    {
        Log.i(TAG, "onClear()")
        disposables.dispose()
        super.onCleared()
    }
}