package com.example.shediz.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shediz.App
import com.example.shediz.R
import com.example.shediz.data.network.Resource
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.TaskType
import com.example.shediz.model.Post
import com.example.shediz.model.User
import com.example.shediz.model.UserFollowMode
import com.example.shediz.view.adapter.ProfileAdapter
import com.example.shediz.view.helper.ItemOffsetDecoration
import com.example.shediz.view.helper.Actions
import com.example.shediz.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile_list.*


class ProfileFragment(private val userName: String) : BaseFragment<Post>()
{
    override val TAG: String = "TAG_" + ProfileFragment::class.simpleName

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var adapter: ProfileAdapter

    private val postsObserver = Observer<Resource<List<Post>?>> {
        Log.i(TAG, "${it.status}: ${it.data?.size}")

        super.handleResult(it)
    }

    private val userObserver = Observer<Resource<User?>> { handleUserInfo(it) }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_list, container, false)

    override fun getRecyclerView(): RecyclerView
    {
        val layoutManager = GridLayoutManager(requireContext(), 3)
        layoutManager.spanSizeLookup = object : SpanSizeLookup()
        {
            override fun getSpanSize(position: Int): Int
            {
                return if (position == 0) 3 else 1
            }
        }

        profileRV.layoutManager = layoutManager
        profileRV.addItemDecoration(ItemOffsetDecoration(2))
        profileRV.adapter = adapter
        profileRV.setHasFixedSize(false)
        profileRV.isNestedScrollingEnabled = false

        return profileRV
    }

    override fun getProgressBar(): ProgressBar? = progressProfile

    override fun getRefreshLayout(): SwipeRefreshLayout? = refreshLayoutProfile

    override fun initAndObserve()
    {
        (activity as MainActivity).setToolbarTitle(userName)

        val actions = Actions(this, requireActivity().supportFragmentManager)

        adapter = ProfileAdapter(requireContext(), ArrayList())
        adapter.setOnItemClickListener { pos, _ ->
            actions.openSinglePost(adapter.getItem(pos))
        }

        adapter.setOnFollowingClicked {
            actions.openFollowFragment(userName, false)
        }

        adapter.setOnFollowersClicked {
            actions.openFollowFragment(userName, true)
        }

        adapter.setOnProfileButtonClicked {
            adapter.getUser()?.apply {
                when (followMode)
                {
                    UserFollowMode.SELF -> startActivity(Intent(requireContext(), EditActivity::class.java))
                    UserFollowMode.FOLLOW -> viewModel.requestFollow(userName)
                    UserFollowMode.UN_FOLLOW -> viewModel.requestUnFollow(userName)
                    UserFollowMode.REQUEST_FOLLOW ->
                    {
                        Toast.makeText(requireContext(), resources.getString(R.string.follow_request_sent),
                            Toast.LENGTH_SHORT).show()

                        viewModel.requestFollow(userName)
                    }
                }
            }
        }

        viewModel.postsLiveData.observe(viewLifecycleOwner, postsObserver)
        viewModel.userLiveData.observe(viewLifecycleOwner, userObserver)
        viewModel.taskLiveData.observe(viewLifecycleOwner, Observer { handleTaskResult(it) })

        viewModel.loadUserInfo(userName)
    }

    override fun loadData(page: Int)
    {
        val loggedInUserName = App.instance.prefs.getUserName()

        if (loggedInUserName == userName)
            viewModel.loadCurrentUserPosts(page)
        else
            viewModel.loadAnotherUserPosts(userName, page)
    }

    override fun clearAdapter() = adapter.clear()

    override fun addToAdapter(data: List<Post>?) = adapter.add(data!!)

    override fun setInRange(items: List<Post>, start: Int, end: Int) = adapter.setInRange(items, start, end)

    private fun handleTaskResult(result: Resource<TaskResult?>)
    {
        val status = result.status

        when
        {
            status.isSuccessful() ->
            {
                when (result.data?.type)
                {
                    TaskType.USER_FOLLOW ->
                    {
                        if (result.data.success)
                            adapter.setUserFollowMode(UserFollowMode.UN_FOLLOW)
                    }
                    TaskType.USER_UNFOLLOW ->
                    {
                        if (result.data.success)
                            adapter.setUserFollowMode(UserFollowMode.FOLLOW)
                    }
                    TaskType.USER_FOLLOW_REQUEST ->
                    {

                    }
                    else -> Log.e(TAG, "Unspecified type: ${result.data?.type}")
                }
            }
            status.isError() ->
            {
                Log.e(TAG, "Error: ${result.error?.message}")

                Toast.makeText(requireActivity(), "Task failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleUserInfo(result: Resource<User?>)
    {
        if (result.status.isSuccessful())
            adapter.setUser(result.data)
        else if (result.status.isError())
            super.handleError(result.error)
    }

    override fun onResume()
    {
        super.onResume()

        view?.apply {
            isFocusableInTouchMode = true
            requestFocus()
        }
    }
}