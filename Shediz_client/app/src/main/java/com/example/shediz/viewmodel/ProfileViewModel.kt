package com.example.shediz.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shediz.data.network.Resource
import com.example.shediz.data.network.TaskResult
import com.example.shediz.model.Post
import com.example.shediz.model.User
import com.example.shediz.repository.ProfileRepository
import io.reactivex.disposables.CompositeDisposable

class ProfileViewModel: ViewModel()
{
    companion object
    {
        private val TAG = "TAG_" + ProfileViewModel::class.simpleName
    }

    private val disposables = CompositeDisposable()

    val postsLiveData = MutableLiveData<Resource<List<Post>?>>()

    val userLiveData = MutableLiveData<Resource<User?>>()

    val taskLiveData = MutableLiveData<Resource<TaskResult>>()

    private val repository = ProfileRepository(postsLiveData, userLiveData, taskLiveData, disposables)

    //To public user
    fun requestFollow(userName: String)
    {
        repository.requestFollow(userName)
    }

    fun requestUnFollow(userName: String)
    {
        repository.requestUnFollow(userName)
    }

    fun loadUserInfo(userName: String)
    {
        repository.loadFullUser(userName)
    }

    fun loadCurrentUserPosts(page: Int)
    {
        repository.loadCurrentUserPosts(page)
    }

    fun loadAnotherUserPosts(userName: String, page: Int)
    {
        repository.loadAnotherUserPosts(userName, page)
    }

    override fun onCleared()
    {
        Log.i(TAG, "onClear()")
        disposables.dispose()
        super.onCleared()
    }
}