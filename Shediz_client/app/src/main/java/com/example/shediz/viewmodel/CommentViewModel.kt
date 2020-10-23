package com.example.shediz.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.Resource
import com.example.shediz.model.Comment
import com.example.shediz.repository.CommentRepository
import io.reactivex.disposables.CompositeDisposable

class CommentViewModel : ViewModel()
{
    companion object
    {
        private val TAG = "TAG_" + CommentViewModel::class.simpleName
    }

    private val disposables = CompositeDisposable()

    val commentLiveData = MutableLiveData<Resource<List<Comment>?>>()

    val taskLiveData = MutableLiveData<Resource<TaskResult>>()

    private val repository = CommentRepository(commentLiveData, taskLiveData, disposables)

    fun addComment(postId: String, text: String)
    {
        repository.createComment(postId, text)
    }

    fun deleteComment(postId: String, commentId: Long)
    {
        repository.removeComment(postId, commentId)
    }

    fun loadComments(postId: String, page: Int)
    {
        repository.loadComments(postId, page)
    }

    override fun onCleared()
    {
        Log.i(TAG, "onClear()")
        disposables.dispose()
        super.onCleared()
    }
}