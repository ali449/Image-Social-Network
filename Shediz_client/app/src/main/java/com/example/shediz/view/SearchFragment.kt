package com.example.shediz.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.ToggleButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shediz.R
import com.example.shediz.data.network.Resource
import com.example.shediz.model.Post
import com.example.shediz.model.User
import com.example.shediz.view.adapter.PostGridAdapter
import com.example.shediz.view.adapter.UserAdapter
import com.example.shediz.view.helper.ItemOffsetDecoration
import com.example.shediz.view.helper.Actions
import com.example.shediz.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.fragment_search.*


class SearchFragment : BaseFragment<Post>()
{
    override val TAG: String = "TAG_" + SearchFragment::class.simpleName

    private lateinit var suggestAdapter: ArrayAdapter<String>

    private lateinit var postsAdapter: PostGridAdapter

    private lateinit var usersAdapter: UserAdapter

    private val postsObserver = Observer<Resource<List<Post>?>> {
        Log.i(TAG, "${it.status}: ${it.data?.size}")

        super.handleResult(it)
    }

    private val userObserver = Observer<Resource<List<User>?>> { handleUserResult(it) }

    private val viewModel: SearchViewModel by viewModels()

    /*
        0 -> Search in whole posts
        1 -> Search in tags
        2 -> Search users
     */
    private var searchType = 0

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_search, container, false)

    override fun getRecyclerView(): RecyclerView
    {
        searchRV.layoutManager = GridLayoutManager(requireContext(), 3)
        searchRV.addItemDecoration(ItemOffsetDecoration(2))
        searchRV.setHasFixedSize(false)
        searchRV.isNestedScrollingEnabled = false

        searchRV.adapter = postsAdapter

        return searchRV
    }

    override fun getProgressBar(): ProgressBar? = progressSearch

    override fun getRefreshLayout(): SwipeRefreshLayout? = refreshLayoutSearch

    override fun initAndObserve()
    {
        toggleBtn0.setOnClickListener { onToggleColor(it) }
        toggleBtn1.setOnClickListener { onToggleColor(it) }
        toggleBtn2.setOnClickListener { onToggleColor(it) }

        postsAdapter = PostGridAdapter(requireContext(), ArrayList())

        usersAdapter = UserAdapter(requireContext(), ArrayList())

        val postAction = Actions(this, requireActivity().supportFragmentManager)

        postsAdapter.setOnItemClickListener { pos, _ ->
            if (searchType == 1)
                viewModel.updateVisitedTag(autoCompleteTV.text.toString(), 1)

            postAction.openSinglePost(postsAdapter.getItem(pos))
        }

        usersAdapter.setOnItemClickListener { pos, _ ->
            postAction.openProfileFragment(usersAdapter.getItem(pos).userName)
        }

        viewModel.postsLiveData.observe(viewLifecycleOwner, postsObserver)

        viewModel.userLiveData.observe(viewLifecycleOwner, userObserver)

        initObserveAutoCompleteTag()
    }

    override fun loadData(page: Int)
    {
        if (searchType != 2 && (page != 0 || refreshLayoutSearch.isRefreshing))
            doSearch(autoCompleteTV.text.toString(), page)
    }

    private fun doSearch(text: String, page: Int)
    {
        if (searchType == 1)
            viewModel.updateVisitedTag(autoCompleteTV.text.toString(), 1)
        when (searchType)
        {
            0 -> viewModel.searchInAllPosts(text, page)
            1 -> viewModel.searchTag(text, page)
            2 -> viewModel.searchUser(text)
            else -> error("Unspecified search type $searchType")
        }
    }

    override fun clearAdapter()
    {
        postsAdapter.clear()
    }

    override fun addToAdapter(data: List<Post>?)
    {
        postsAdapter.add(data!!)
    }

    override fun setInRange(items: List<Post>, start: Int, end: Int)
    {
        postsAdapter.setInRange(items, start, end)
    }

    private fun handleUserResult(result: Resource<List<User>?>)
    {
        val status = result.status

        if (status.isSuccessful())
        {
            usersAdapter.clear()
            if (!result.data.isNullOrEmpty())
                usersAdapter.add(result.data)
        }
    }

    private fun initObserveAutoCompleteTag()
    {
        suggestAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        autoCompleteTV.setAdapter(suggestAdapter)

        autoCompleteTV.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || (event.action == KeyEvent.ACTION_DOWN && actionId == EditorInfo.IME_NULL))
            {
                super.resetData()
                doSearch(autoCompleteTV.text.toString(), 0)

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        //If clicked on suggested tags
        autoCompleteTV.setOnItemClickListener { _, _, position, _ ->
            super.resetData()

            doSearch(suggestAdapter.getItem(position)!!, 0)
        }

        autoCompleteTV.addTextChangedListener(object : TextWatcher
        {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {
                if (searchType != 1)
                    return

                if (s.isBlank())
                    suggestAdapter.clear()
                else if (!s.contains(' '))
                    viewModel.suggestTags(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        //Observe suggested tags from server
        viewModel.strLiveData.observe(viewLifecycleOwner, Observer {
            val status = it.status

            if (status.isSuccessful())
            {
                if (it.data.isNullOrEmpty())
                    suggestAdapter.clear()
                else
                    setAutoCompleteData(it.data)
            }
            else if (status.isError())
                suggestAdapter.clear()
        })
    }

    private fun setAutoCompleteData(list: List<String>)
    {
        suggestAdapter.clear()
        suggestAdapter.addAll(list)
    }

    private fun onToggleColor(view: View)
    {
        val previousSearchType = searchType
        for (i in 0 until searchGroup.childCount)
        {
            val toggleButton = searchGroup.getChildAt(i) as ToggleButton
            if (toggleButton.id == view.id)
            {
                toggleButton.isChecked = true
                searchType = i
            }
            else
                toggleButton.isChecked = false
        }

        if (previousSearchType != searchType)
            super.resetData()

        if (searchType == 2)
        {
            searchRV.layoutManager = LinearLayoutManager(requireContext())
            searchRV.adapter = usersAdapter
        }
        else
        {
            searchRV.layoutManager = GridLayoutManager(requireContext(), 3)
            searchRV.adapter = postsAdapter
        }
    }
}