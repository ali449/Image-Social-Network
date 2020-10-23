package com.example.shediz.repository

import androidx.lifecycle.MutableLiveData
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.Resource
import com.example.shediz.model.Comment
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class CommentRepository(private val result: MutableLiveData<Resource<List<Comment>?>>,
                        private val resultTask: MutableLiveData<Resource<TaskResult>>,
                        private val disposables: CompositeDisposable)
{
    fun createComment(postId: String, text: String)
    {
        resultTask.value = Resource.loading()

        ApiService.createComment(postId, text).subscribe(object : SingleObserver<TaskResult>
        {
            override fun onSuccess(t: TaskResult)
            {
                resultTask.value = Resource.success(t)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultTask.value = Resource.error(e)
            }
        })
    }

    fun removeComment(postId: String, commentId: Long)
    {
        resultTask.value = Resource.loading()

        ApiService.removeComment(commentId, postId).subscribe(object : SingleObserver<TaskResult>
        {
            override fun onSuccess(t: TaskResult)
            {
                resultTask.value = Resource.success(t)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                resultTask.value = Resource.error(e)
            }
        })
    }

    fun loadComments(postId: String, page: Int)
    {
        result.value = Resource.loading()

        ApiService.getComments(postId, page).subscribe(object : SingleObserver<List<Comment>>
        {
            override fun onSuccess(t: List<Comment>)
            {
                result.value = Resource.success(t, page)
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