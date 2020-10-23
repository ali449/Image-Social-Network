package com.example.shediz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shediz.data.network.Resource
import com.example.shediz.model.User
import com.example.shediz.repository.FollowRepository
import io.reactivex.disposables.CompositeDisposable

class FollowViewModel : ViewModel()
{
    companion object
    {
        private val TAG = "TAG_" + FollowViewModel::class.simpleName
    }

    private val disposables = CompositeDisposable()

    val userLiveData = MutableLiveData<Resource<List<User>?>>()

    private val repository = FollowRepository(userLiveData, disposables)

    fun loadFollowing(userName: String, page: Int)
    {
        repository.loadFollowing(userName, page)
    }

    fun loadFollowers(userName: String, page: Int)
    {
        repository.loadFollowers(userName, page)
    }
}