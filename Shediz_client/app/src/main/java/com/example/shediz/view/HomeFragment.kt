package com.example.shediz.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shediz.R
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.Resource
import com.example.shediz.data.network.TaskType
import com.example.shediz.model.Post
import com.example.shediz.view.adapter.PostFullAdapter
import com.example.shediz.view.helper.OnListActionListener
import com.example.shediz.view.helper.Actions
import com.example.shediz.viewmodel.FeedViewModel
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : BaseFragment<Post>(), OnListActionListener
{
    override val TAG: String = "TAG_" + HomeFragment::class.simpleName

    private val viewModel: FeedViewModel by viewModels()

    private lateinit var adapter: PostFullAdapter

    private lateinit var postAction: Actions

    private val postsObserver = Observer<Resource<List<Post>?>> {
        Log.i(TAG, "${it.status}: ${it.data?.size}")

        super.handleResult(it)
    }

    private val taskObserver = Observer<Resource<TaskResult?>> {
        handleTaskResult(it)
    }
    
    override fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_home, container, false)
    
    override fun getRecyclerView(): RecyclerView
    {
        feedRV.layoutManager = LinearLayoutManager(requireContext())
        feedRV.isNestedScrollingEnabled = false
        feedRV.adapter = adapter
        return feedRV
    }

    override fun getProgressBar(): ProgressBar? = progressBar

    override fun getRefreshLayout(): SwipeRefreshLayout? = refreshLayout

    override fun initAndObserve()
    {
        //Initialize adapter, because this function called first in base
        adapter = PostFullAdapter(requireContext(), ArrayList())
        adapter.setListener(this)

        postAction = Actions(this, requireActivity().supportFragmentManager)

        viewModel.postsLiveData.observe(viewLifecycleOwner, postsObserver)
        viewModel.taskLiveData.observe(viewLifecycleOwner, taskObserver)
    }

    override fun loadData(page: Int) = viewModel.loadFeed(page)

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
                    TaskType.POST_LIKED -> adapter.setPostLikeChanged(result.data.pid!!, true)
                    TaskType.POST_UNLIKED -> adapter.setPostLikeChanged(result.data.pid!!, false)
                    TaskType.POST_REMOVED -> if (result.data.success) adapter.removeItemById(result.data.pid!!)
                    else -> Log.e(TAG, "Unspecified data type")
                }
            }
            status.isError() ->
            {
                handleError(result.error)
                Toast.makeText(requireActivity(), "Task failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onUserClickListener(position: Int, view: View)
    {
        val clickedUserName = adapter.getItem(position).userName

        (activity as MainActivity).setToolbarTitle(clickedUserName)

        postAction.openProfileFragment(clickedUserName)
    }

    override fun onLikeClickListener(position: Int, view: View)
    {
        val isUserLiked = adapter.getItem(position).isUserLiked ?: false

        if (isUserLiked)
            viewModel.requestUnLikePost(adapter.getItem(position).id)
        else
            viewModel.requestLikePost(adapter.getItem(position).id)
    }

    override fun onCommentClickListener(position: Int, view: View)
    {
        val clickedPid = adapter.getItem(position).id

        (activity as MainActivity).setToolbarTitle(requireContext().resources.getString(R.string.comments))

        postAction.openCommentFragment(clickedPid)
    }

    override fun onShareClickListener(position: Int, view: View)
    {
        postAction.shareImage(adapter.getItem(position).id)
    }

    override fun onOptionsClickListener(position: Int, view: View)
    {
        postAction.openOptionsDialog(adapter.getItem(position)) { viewModel.requestDeletePost(it) }
    }
}