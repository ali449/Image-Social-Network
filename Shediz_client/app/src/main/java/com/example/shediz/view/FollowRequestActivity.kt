package com.example.shediz.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.androidnetworking.error.ANError
import com.example.shediz.R
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.Resource
import com.example.shediz.data.network.TaskResult
import com.example.shediz.data.network.TaskType
import com.example.shediz.model.User
import com.example.shediz.viewmodel.RequestViewModel
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_follow.*

class FollowRequestActivity : AppCompatActivity()
{
    companion object
    {
        private val TAG = "tag_" + FollowRequestActivity::class.simpleName
    }

    private var sourceUserName: String? = null

    private val viewModel: RequestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)

        sourceUserName = intent.getStringExtra("sourceUserName")

        if (sourceUserName.isNullOrBlank())
            finish()

        acceptBtn.setOnClickListener { viewModel.notifyFollowRequestResult(sourceUserName!!, true) }

        rejectBtn.setOnClickListener { viewModel.notifyFollowRequestResult(sourceUserName!!, false) }

        viewModel.userLiveData.observe(this, Observer { handleUserInfo(it) })
        viewModel.taskLiveData.observe(this, Observer { handleTaskResult(it) })

        viewModel.loadUser(sourceUserName!!)
        ApiService.loadProfileImage(this, sourceUserName!!).into(profileImgR)
    }

    private fun handleTaskResult(result: Resource<TaskResult?>)
    {
        if (result.status.isSuccessful())
        {
            if (result.data!!.type == TaskType.USER_FOLLOW_REQUEST)
                finish()
            else
                Toast.makeText(this, "Task failed!", Toast.LENGTH_SHORT).show()
        }
        else if (result.status.isError())
        {
            Toast.makeText(this, "Task failed!", Toast.LENGTH_SHORT).show()

            val e = result.error!!
            if (e is ANError)
                Log.e(TAG, "Error: ${e.errorCode}, Body: ${e.errorBody}, Details: ${e.errorDetail}")
            else
                Log.e(TAG, "Error: ${e.message}")
        }
    }

    private fun handleUserInfo(result: Resource<User?>)
    {
        if (result.status.isSuccessful())
        {
            if (result.data != null)
            {
                userNameTxtR.text = result.data.userName
                bioTxtR.text = result.data.bio
            }
        }
        else if (result.status.isError())
        {
            Toast.makeText(this, "Load user info failed!", Toast.LENGTH_SHORT).show()

            val e = result.error!!
            if (e is ANError)
                Log.e(TAG, "Error: ${e.errorCode}, Body: ${e.errorBody}, Details: ${e.errorDetail}")
            else
                Log.e(TAG, "Error: ${e.message}")
        }
    }
}