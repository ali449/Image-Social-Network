package com.example.shediz.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shediz.R
import com.example.shediz.data.network.Resource
import com.example.shediz.model.User
import com.example.shediz.view.adapter.UserAdapter
import com.example.shediz.viewmodel.FollowViewModel
import kotlinx.android.synthetic.main.fragment_follow.*


class FollowFragment(private val useName: String, private val isFollowersMode: Boolean) : BaseFragment<User>()
{
    override val TAG: String = "TAG_" + FollowFragment::class.simpleName

    private lateinit var adapter: UserAdapter

    private val userObserver = Observer<Resource<List<User>?>> { super.handleResult(it) }

    private val viewModel: FollowViewModel by viewModels()

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_follow, container, false)

    override fun getRecyclerView(): RecyclerView
    {
        followRV.layoutManager = LinearLayoutManager(requireContext())
        followRV.setHasFixedSize(false)
        followRV.isNestedScrollingEnabled = false

        followRV.adapter = adapter

        return followRV
    }

    override fun getProgressBar(): ProgressBar? = progressFollow

    override fun getRefreshLayout(): SwipeRefreshLayout? = refreshLayoutFollow

    override fun initAndObserve()
    {
        adapter = UserAdapter(requireContext(), ArrayList())

        viewModel.userLiveData.observe(viewLifecycleOwner, userObserver)
    }

    override fun loadData(page: Int)
    {
        if (isFollowersMode)
            viewModel.loadFollowers(useName, page)
        else
            viewModel.loadFollowing(useName, page)
    }

    override fun clearAdapter()
    {
        adapter.clear()
    }

    override fun addToAdapter(data: List<User>?)
    {
        adapter.add(data!!)
    }

    override fun setInRange(items: List<User>, start: Int, end: Int)
    {
        adapter.setInRange(items, start, end)
    }

}