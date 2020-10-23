package com.example.shediz.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.Resource
import com.example.shediz.data.network.TaskResult
import com.example.shediz.model.User
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class RequestViewModel : ViewModel()
{
    companion object
    {
        private val TAG = "TAG_" + RequestViewModel::class.simpleName
    }

    private val disposables = CompositeDisposable()

    val userLiveData = MutableLiveData<Resource<User?>>()

    val taskLiveData = MutableLiveData<Resource<TaskResult>>()


    fun notifyFollowRequestResult(userName: String, hasAccept: Boolean)
    {
        taskLiveData.value = Resource.loading()

        ApiService.notifyFollowResult(userName, hasAccept).subscribe(object : SingleObserver<TaskResult>
        {
            override fun onSuccess(t: TaskResult)
            {
                taskLiveData.value = Resource.success(t)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                taskLiveData.value = Resource.error(e)
            }
        })
    }

    fun loadUser(userName: String)
    {
        userLiveData.value = Resource.loading()

        ApiService.getUser(userName).subscribe(object : SingleObserver<User>
        {
            override fun onSuccess(t: User)
            {
                userLiveData.value = Resource.success(t)
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                userLiveData.value = Resource.error(e)
            }
        })
    }

    override fun onCleared()
    {
        Log.i(TAG, "onClear()")
        disposables.dispose()
        super.onCleared()
    }
}