package com.example.shediz.repository

import androidx.lifecycle.MutableLiveData
import com.example.shediz.App
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.Resource
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.UserWithFollow
import com.example.shediz.model.Post
import com.example.shediz.model.User
import com.example.shediz.model.UserFollowMode
import com.example.shediz.utils.Constants.PAGE_SIZE
import com.example.shediz.utils.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.Executors

class ProfileRepository(private val result: MutableLiveData<Resource<List<Post>?>>,
                        private val resultUser: MutableLiveData<Resource<User?>>,
                        private val resultTask: MutableLiveData<Resource<TaskResult>>,
                        private val disposables: CompositeDisposable)
{
    private val dao = App.instance.db.postDao()

    private val taskSubscriber = object : SingleObserver<TaskResult>
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
            resultTask.value = Resource.error(e, null)
        }
    }

    //To public user
    fun requestFollow(userName: String)
    {
        resultTask.value = Resource.loading()

        ApiService.requestFollow(userName).subscribe(taskSubscriber)
    }

    fun requestUnFollow(userName: String)
    {
        resultTask.value = Resource.loading()

        ApiService.requestUnFollow(userName).subscribe(taskSubscriber)
    }

    fun loadFullUser(userName: String)
    {
        resultUser.value = Resource.loading()

        ApiService.getUserWithFollowStatus(userName).subscribe(object : SingleObserver<UserWithFollow>
        {
            override fun onSuccess(t: UserWithFollow)
            {
                val currentUserName = App.instance.prefs.getUserName()

                val followMode: UserFollowMode = when
                {
                    userName == currentUserName -> UserFollowMode.SELF
                    t.isFollowing -> UserFollowMode.UN_FOLLOW
                    t.isPrivate -> UserFollowMode.REQUEST_FOLLOW
                    else -> UserFollowMode.FOLLOW
                }

                val user = User(t.userName, t.isPrivate, t.bio, t.numFollowing, t.numFollowers, t.isEnabled, followMode)

                resultUser.value = Resource.success(user)
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

    fun loadCurrentUserPosts(page: Int)
    {
        App.instance.prefs.getUserName()?.let { loadPosts(it, page, true) }
    }

    fun loadAnotherUserPosts(userName: String, page: Int)
    {
        loadPosts(userName, page, false)
    }

    private fun loadPosts(userName: String, page: Int, isCurrent: Boolean)
    {
        result.value = Resource.loading(null, page)

        if (isCurrent)
            Executors.newCachedThreadPool().execute {
                result.postValue(Resource.success(dao.loadLimitedUserPosts(PAGE_SIZE, PAGE_SIZE * page), page))
            }

        if (!Util.isNetworkAvailable(App.instance))
            return

        ApiService.getUserPosts(userName, page).subscribe(object : SingleObserver<List<Post>>
        {
            override fun onSuccess(response: List<Post>)
            {
                result.value = Resource.success(response, page)

                if (isCurrent)
                {
                    val mappedResult = response.map {
                        it.type = 1
                        it
                    }

                    Executors.newCachedThreadPool().execute {
                        dao.addPosts(mappedResult)
                    }
                }
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                result.value = Resource.error(e, null)
            }
        })
    }
}