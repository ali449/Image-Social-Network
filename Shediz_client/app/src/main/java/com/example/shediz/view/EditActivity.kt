package com.example.shediz.view

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shediz.R.layout.activity_edit
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.TaskResponse
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_edit.*

class EditActivity : AppCompatActivity()
{
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(activity_edit)

        editBtn.setOnClickListener {
            progressEdit.visibility = View.VISIBLE
            ApiService.editUser(inputEdit.text.toString(), isPrivateEdit.isChecked).subscribe(object : SingleObserver<TaskResponse>
            {
                override fun onSuccess(t: TaskResponse)
                {
                    progressEdit.visibility = View.GONE
                    if (t.success)
                    {
                        Toast.makeText(this@EditActivity, "Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else
                        Toast.makeText(this@EditActivity, "Try again", Toast.LENGTH_SHORT).show()
                }

                override fun onSubscribe(d: Disposable)
                {
                    disposables.addAll(d)
                }

                override fun onError(e: Throwable)
                {
                    progressEdit.visibility = View.GONE
                    Toast.makeText(this@EditActivity, "Error", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            })
        }
    }

    override fun onStop()
    {
        super.onStop()
        disposables.dispose()
    }
}