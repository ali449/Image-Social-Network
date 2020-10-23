package com.example.shediz.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shediz.App
import com.example.shediz.R
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.Resource
import com.example.shediz.data.network.TaskType
import com.example.shediz.model.Comment
import com.example.shediz.view.adapter.CommentAdapter
import com.example.shediz.viewmodel.CommentViewModel
import kotlinx.android.synthetic.main.fragment_comment.*


class CommentFragment(private val postId: String): BaseFragment<Comment>()
{
    override val TAG: String = "TAG_" + CommentFragment::class.simpleName

    private lateinit var adapter: CommentAdapter

    private val viewModel: CommentViewModel by viewModels()

    private val commentObserver = Observer<Resource<List<Comment>?>> { super.handleResult(it) }

    private val taskObserver = Observer<Resource<TaskResult?>> { handleTaskResult(it) }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_comment, container, false)

    override fun getRecyclerView(): RecyclerView
    {
        commentRV.layoutManager = LinearLayoutManager(requireContext())
        commentRV.isNestedScrollingEnabled = false
        commentRV.adapter = adapter
        return commentRV
    }

    override fun getProgressBar(): ProgressBar? = progressBarComment

    override fun getRefreshLayout(): SwipeRefreshLayout? = refreshLayoutComment

    override fun initAndObserve()
    {
        adapter = CommentAdapter(requireContext(), ArrayList())

        adapter.setOnItemClickListener { i, _ ->
            if (adapter.getItem(i).userName == App.instance.prefs.getUserName())
                askDeleteComment(i)
        }

        sendCommentBtn.setOnClickListener { viewModel.addComment(postId, inputComment.text.toString()) }

        viewModel.commentLiveData.observe(viewLifecycleOwner, commentObserver)
        viewModel.taskLiveData.observe(viewLifecycleOwner, taskObserver)
    }

    override fun loadData(page: Int)
    {
        viewModel.loadComments(postId, page)
    }

    override fun clearAdapter()
    {
        adapter.clear()
    }

    override fun addToAdapter(data: List<Comment>?)
    {
        adapter.addList(data!!)
    }

    override fun setInRange(items: List<Comment>, start: Int, end: Int)
    {
        adapter.setInRange(items, start, end)
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
                    TaskType.COMMENT_CREATED ->
                    {
                        val currentUserName = App.instance.prefs.getUserName()
                        val text = inputComment.text.toString()
                        val comment = Comment(result.data.cid!!, currentUserName!!, result.data.pid!!, text)

                        adapter.add(comment)
                    }
                    TaskType.COMMENT_REMOVED -> adapter.deleteByCid(result.data.cid!!)
                    else -> Log.e(TAG, "Unspecified data type")
                }
                inputComment.text.clear()
            }
            status.isLoading() ->
            {
                //Log.i(TAG, "Loading like")
            }
            status.isError() ->
            {
                inputComment.text.clear()

                handleError(result.error)
                Toast.makeText(requireActivity(), "Task failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askDeleteComment(clickPosition: Int)
    {
        val mAlertDialog = AlertDialog.Builder(requireContext())
        mAlertDialog.setTitle(resources.getString(R.string.delete_confirm))
        mAlertDialog.setMessage(resources.getString(R.string.are_you_sure))
        mAlertDialog.setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
            viewModel.deleteComment(postId, adapter.getItem(clickPosition).cid)
            dialog.dismiss()
        }
        mAlertDialog.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        mAlertDialog.show()
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