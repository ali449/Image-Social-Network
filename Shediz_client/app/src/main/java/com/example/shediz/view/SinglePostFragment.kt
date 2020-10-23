package com.example.shediz.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.shediz.R
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.Resource
import com.example.shediz.data.network.TaskType
import com.example.shediz.model.Post
import com.example.shediz.view.helper.OnListActionListener
import com.example.shediz.view.helper.Actions
import com.example.shediz.view.helper.PostHolder
import com.example.shediz.viewmodel.SinglePostViewModel

class SinglePostFragment(private val post: Post): Fragment(), OnListActionListener
{
    companion object
    {
        private val TAG: String = "TAG_" + SinglePostFragment::class.simpleName
    }

    private val viewModel: SinglePostViewModel by viewModels()

    private lateinit var rootView: View

    private lateinit var holder: PostHolder

    private lateinit var postAction: Actions

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.item_post_full, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        holder = PostHolder(requireContext(), rootView, this)
        holder.bindView(post)

        (activity as MainActivity).setToolbarTitle(post.userName)

        postAction = Actions(this, requireActivity().supportFragmentManager)

        viewModel.taskLiveData.observe(viewLifecycleOwner, Observer { handleTaskResult(it) })
    }

    //Called by activity, when comment fragment has pooped from back stack
    fun updateTitle()
    {
        (activity as MainActivity).setToolbarTitle(post.userName)
    }

    private fun handleTaskResult(result: Resource<TaskResult?>)
    {
        val status = result.status

        when
        {
            status.isSuccessful() ->
            {
                when (result.data?.type)
                {
                    TaskType.POST_LIKED ->
                    {
                        post.isUserLiked = true
                        holder.bindView(post)
                    }
                    TaskType.POST_UNLIKED ->
                    {
                        post.isUserLiked = false
                        holder.bindView(post)
                    }
                    TaskType.POST_REMOVED ->
                    {
                        if (result.data.success)
                            (activity as MainActivity).onBackPressed()
                    }
                    else -> Log.e(TAG, "Unspecified data type")
                }
            }
            status.isLoading() ->
            {
                //Log.i(TAG, "Loading task")
            }
            status.isError() ->
            {
                Log.e(TAG, "Error: ${result.error?.message}")

                Toast.makeText(requireActivity(), "Task failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onUserClickListener(position: Int, view: View)
    {
        (activity as MainActivity).setToolbarTitle(post.userName)

        postAction.openProfileFragment(post.userName)
    }

    override fun onLikeClickListener(position: Int, view: View)
    {
        val isUserLiked = post.isUserLiked ?: false

        if (isUserLiked)
            viewModel.requestUnLikePost(post.id)
        else
            viewModel.requestLikePost(post.id)
    }

    override fun onCommentClickListener(position: Int, view: View)
    {
        (activity as MainActivity).setToolbarTitle(requireContext().resources.getString(R.string.comments))

        postAction.openCommentFragment(post.id)
    }

    override fun onShareClickListener(position: Int, view: View)
    {
        postAction.shareImage(post.id)
    }

    override fun onOptionsClickListener(position: Int, view: View)
    {
        postAction.openOptionsDialog(post) { viewModel.requestDeletePost(it) }
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