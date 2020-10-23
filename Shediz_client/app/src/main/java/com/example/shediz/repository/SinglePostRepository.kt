package com.example.shediz.repository

import androidx.lifecycle.MutableLiveData
import com.example.shediz.App
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.Resource
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.Executors

class SinglePostRepository(private val resultTask: MutableLiveData<Resource<TaskResult>>,
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
        resultTask.value = Resource.loading()

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
        resultTask.value = Resource.loading()

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
}