package com.example.shediz.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.error.ANError
import com.example.shediz.App
import com.example.shediz.data.network.Resource
import com.example.shediz.utils.Constants.PAGE_SIZE
import com.example.shediz.utils.Util

abstract class BaseFragment<T>: Fragment()
{

    abstract val TAG: String

    //For pagination
    private var requestPage = 0
    private var loadedPage = -1
    private var isLastPageData = false
    private var isLoadingData = false
        set(value)
        {
            field = value
            if (value)
                getProgressBar()?.visibility = View.VISIBLE
            else
                getProgressBar()?.visibility = View.GONE
        }

    protected abstract fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?

    protected abstract fun getRecyclerView(): RecyclerView

    protected abstract fun getProgressBar(): ProgressBar?

    protected abstract fun getRefreshLayout(): SwipeRefreshLayout?

    protected abstract fun initAndObserve()

    protected abstract fun loadData(page: Int)

    protected abstract fun clearAdapter()

    protected abstract fun addToAdapter(data: List<T>?)

    protected abstract fun setInRange(items: List<T>, start: Int, end: Int)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflateView(inflater, container, savedInstanceState)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        initAndObserve()

        loadData(requestPage)

        getRecyclerView().addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
            {
                super.onScrolled(recyclerView, dx, dy)

                //Refresh only in top list
                val topRowVerticalPosition = if (recyclerView.childCount == 0) 0 else recyclerView.getChildAt(0).top
                getRefreshLayout()?.isEnabled = topRowVerticalPosition >= 0


                val layoutManager = recyclerView.layoutManager

                var firstVisibleItemPosition = 0
                if (layoutManager is LinearLayoutManager)
                    firstVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                else if (layoutManager is GridLayoutManager)
                    firstVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                if (isLoadingData || isLastPageData)
                    return

                if (firstVisibleItemPosition != 0 && firstVisibleItemPosition == recyclerView.adapter!!.itemCount-1)
                {
                    loadData(++requestPage)
                }
            }
        })

        getRefreshLayout()?.setOnRefreshListener { loadData(0) }
    }

    protected fun resetData()
    {
        loadedPage = -1
        requestPage = 0
        isLastPageData = false

        clearAdapter()
    }

    protected fun handleResult(result: Resource<List<T>?>)
    {
        val status = result.status

        if (status.isSuccessful())
        {
            isLoadingData = false

            if (getRefreshLayout() != null && getRefreshLayout()!!.isRefreshing)
            {
                getRefreshLayout()!!.isRefreshing = false

                resetData()
            }

            requestPage = result.pageData!!

            if (!result.data.isNullOrEmpty())
            {
                if (loadedPage != requestPage) //If new data received
                {
                    addToAdapter(result.data)
                    loadedPage++
                }
                else //If updated data received
                    setInRange(result.data, requestPage * PAGE_SIZE, result.data.size)

                isLastPageData = result.data.size < PAGE_SIZE
            }
            else if (Util.isNetworkAvailable(App.instance)) isLastPageData = true
            else requestPage = loadedPage
        }
        else if (status.isLoading()) isLoadingData = true
        else if (status.isError())
        {
            isLoadingData = false

            requestPage = loadedPage //or requestPage--

            getRefreshLayout()?.isRefreshing = false

            handleError(result.error)
            Toast.makeText(requireActivity(), "Cannot load feed", Toast.LENGTH_SHORT).show()
        }
    }

    protected open fun handleError(error: Throwable?)
    {
        if (error is ANError?)
            Log.e(TAG, "Error: ${error?.errorCode}, Body: ${error?.errorBody}, Details: ${error?.errorDetail}")
        else
            Log.e(TAG, "Error: ${error?.message}")
    }
}