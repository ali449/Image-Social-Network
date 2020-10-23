package com.example.shediz.repository

import androidx.lifecycle.MutableLiveData
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.Resource
import com.example.shediz.model.User
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class FollowRepository(private val resultUser: MutableLiveData<Resource<List<User>?>>,
                       private val disposables: CompositeDisposable)
{
    fun loadFollowing(userName: String, page: Int)
    {
        resultUser.value = Resource.loading()

        ApiService.getFollowingUserNames(userName, page).subscribe(object : SingleObserver<List<String>>
        {
            override fun onSuccess(t: List<String>)
            {
                val mappedResult = t.map {
                    User(it, false, null, null, null, true, null)
                }

                resultUser.value = Resource.success(mappedResult, page)
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

    fun loadFollowers(userName: String, page: Int)
    {
        resultUser.value = Resource.loading()

        ApiService.getFollowersUserNames(userName, page).subscribe(object : SingleObserver<List<String>>
        {
            override fun onSuccess(t: List<String>)
            {
                val mappedResult = t.map {
                    User(it, false, null, null, null, true, null)
                }

                resultUser.value = Resource.success(mappedResult, page)
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
}