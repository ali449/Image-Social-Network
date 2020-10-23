package com.example.shediz.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shediz.R
import com.example.shediz.data.network.Resource
import com.example.shediz.model.Post
import com.example.shediz.view.adapter.PostGridAdapter
import com.example.shediz.view.helper.Actions
import com.example.shediz.view.helper.ItemOffsetDecoration
import com.example.shediz.viewmodel.RecViewModel
import kotlinx.android.synthetic.main.fragment_rec.*
import kotlin.collections.ArrayList


class RecFragment : BaseFragment<Post>()
{
    override val TAG: String = "TAG_" + RecFragment::class.simpleName

    private lateinit var adapter: PostGridAdapter

    private val postsObserver = Observer<Resource<List<Post>?>> { super.handleResult(it) }

    private val viewModel: RecViewModel by viewModels()

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_rec, container, false)

    override fun getRecyclerView(): RecyclerView
    {
        recRV.layoutManager = GridLayoutManager(requireContext(), 3)
        recRV.addItemDecoration(ItemOffsetDecoration(2))
        recRV.setHasFixedSize(false)
        recRV.isNestedScrollingEnabled = false

        recRV.adapter = adapter

        return recRV
    }

    override fun getProgressBar(): ProgressBar? = progressRec

    override fun getRefreshLayout(): SwipeRefreshLayout? = refreshLayoutRec

    override fun initAndObserve()
    {
        adapter = PostGridAdapter(requireContext(), ArrayList())

        val actions = Actions(this, requireActivity().supportFragmentManager)
        adapter.setOnItemClickListener { pos, _ ->
            actions.openSinglePost(adapter.getItem(pos))
        }

        viewModel.postsLiveData.observe(viewLifecycleOwner, postsObserver)
    }

    override fun loadData(page: Int)
    {
        viewModel.recommendMe(page)
    }

    override fun clearAdapter()
    {
        adapter.clear()
    }

    override fun addToAdapter(data: List<Post>?)
    {
        adapter.add(data!!)
    }

    override fun setInRange(items: List<Post>, start: Int, end: Int)
    {
        adapter.setInRange(items, start, end)
    }
}