package com.example.shediz.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.shediz.App
import com.example.shediz.R.layout.activity_setting
import com.example.shediz.R.string
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.TaskResponse
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_setting.*


class SettingActivity : AppCompatActivity()
{
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(activity_setting)

        clearBtn.setOnClickListener { askClearHistory() }

        logoutBtn.setOnClickListener { askLogout() }
    }

    private fun askClearHistory()
    {
        val mAlertDialog = AlertDialog.Builder(this)
        mAlertDialog.setTitle(resources.getString(string.delete_confirm))
        mAlertDialog.setMessage(resources.getString(string.are_you_sure))
        mAlertDialog.setPositiveButton(resources.getString(string.yes)) { dialog, _ ->
            requestClearHistory()
            dialog.dismiss()
        }
        mAlertDialog.setNegativeButton(resources.getString(string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        mAlertDialog.show()
    }

    private fun askLogout()
    {
        val mAlertDialog = AlertDialog.Builder(this)
        mAlertDialog.setTitle(resources.getString(string.logout))
        mAlertDialog.setMessage(resources.getString(string.are_you_sure))
        mAlertDialog.setPositiveButton(resources.getString(string.yes)) { dialog, _ ->
            requestLogout()
            dialog.dismiss()
        }
        mAlertDialog.setNegativeButton(resources.getString(string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        mAlertDialog.show()
    }

    private fun requestClearHistory()
    {
        ApiService.deleteVisitedTags().subscribe(object : SingleObserver<String>
        {
            override fun onSuccess(t: String)
            {
                Log.i("TAG_SettingActivity", t)
                Toast.makeText(this@SettingActivity, "Successfully", Toast.LENGTH_SHORT).show()
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                Toast.makeText(this@SettingActivity, "Error", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        })
    }

    private fun requestLogout()
    {
        ApiService.logout().subscribe(object: SingleObserver<TaskResponse>
        {
            override fun onSuccess(t: TaskResponse)
            {
                if (t.success)
                {
                    App.instance.prefs.setTokenAndUserName("", "")
                    ApiService.token = ""

                    val homeIntent = Intent(Intent.ACTION_MAIN)
                    homeIntent.addCategory(Intent.CATEGORY_HOME)
                    homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(homeIntent)
                }
                else
                    Toast.makeText(this@SettingActivity, "Error", Toast.LENGTH_SHORT).show()
            }

            override fun onSubscribe(d: Disposable)
            {

            }

            override fun onError(e: Throwable)
            {
                Toast.makeText(this@SettingActivity, "Error", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        })
    }

    override fun onStop()
    {
        super.onStop()
        disposables.dispose()
    }
}