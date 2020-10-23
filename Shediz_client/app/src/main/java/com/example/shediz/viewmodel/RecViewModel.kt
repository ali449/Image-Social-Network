package com.example.shediz.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shediz.data.network.Resource
import com.example.shediz.model.Post
import com.example.shediz.repository.RecRepository
import io.reactivex.disposables.CompositeDisposable

class RecViewModel : ViewModel()
{
    companion object
    {
        private val TAG = "TAG_" + RecViewModel::class.simpleName
    }

    val postsLiveData = MutableLiveData<Resource<List<Post>?>>()

    private val disposables = CompositeDisposable()

    private val repository = RecRepository(postsLiveData, disposables)

    fun recommendMe(page: Int)
    {
        repository.recommendMe(page)
    }

    override fun onCleared()
    {
        Log.i(TAG, "onClear()")
        disposables.dispose()
        super.onCleared()
    }
}