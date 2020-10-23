package com.example.shediz.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.error.ANError
import com.example.shediz.App
import com.example.shediz.R
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.LoginResponse
import com.example.shediz.data.network.TaskResponse
import com.example.shediz.utils.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.io.IOException


class LoginActivity : AppCompatActivity()
{
    companion object
    {
        private val TAG = "TAG_" + LoginActivity::class.simpleName
    }

    //For switch between login/register mode
    private var isLoginMode = true

    private var profilePicFile: File? = null

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        if (!App.instance.prefs.getUserToken().isNullOrBlank())
        {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        //Switch between login and register mode
        bottomTextView.setOnClickListener {
            if (isLoginMode)
            {
                //Now go to register mode
                title = resources.getString(R.string.register)
                bottomTextView.text = resources.getString(R.string.login_account)
                profilePic.visibility = View.VISIBLE
                parentConfirmPass.visibility = View.VISIBLE
                parentBio.visibility = View.VISIBLE
                isPrivateBox.visibility = View.VISIBLE
                submitBtn.text = resources.getString(R.string.register)
            }
            else
            {
                //Now go to login mode
                title = resources.getString(R.string.login_account)
                bottomTextView.text = resources.getString(R.string.create_account)
                profilePic.visibility = View.GONE
                parentConfirmPass.visibility = View.GONE
                parentBio.visibility = View.GONE
                isPrivateBox.visibility = View.GONE
                submitBtn.text = resources.getString(R.string.sign_in)
            }
            isLoginMode = !isLoginMode
        }

        submitBtn.setOnClickListener {
            val dialog: AlertDialog

            val userName = inputUsername.text.toString().trim()
            val passWord = inputPassword.text.toString().trim()

            if (userName.isEmpty() || passWord.isEmpty())
                return@setOnClickListener

            if (isLoginMode)
            {
                dialog = createProgressDialog(resources.getString(R.string.signing_in))

                requestLogin(userName, passWord, dialog)
            }
            else
            {
                val confirmPassWord = inputConfirmPassword.text.toString().trim()
                val bio = inputBio.text.toString().trim()

                if (confirmPassWord.isEmpty() || passWord.length != confirmPassWord.length)
                    return@setOnClickListener

                dialog = createProgressDialog(resources.getString(R.string.registering))

                requestRegister(userName, passWord, bio, isPrivateBox.isChecked, dialog)
            }

            dialog.show()
        }
    }

    fun uploadProfilePic()
    {
        if (profilePicFile == null)
            return

        ApiService.uploadProfilePic(profilePicFile!!).subscribe(object : SingleObserver<TaskResponse>
        {
            override fun onSuccess(t: TaskResponse)
            {
                Log.d(TAG, "upload-onSuccess: $t")

                if (t.success)
                    Log.d(TAG, "upload-onSuccess: Uploaded successfully")
            }

            override fun onSubscribe(d: Disposable)
            {
                disposables.addAll(d)
            }

            override fun onError(e: Throwable)
            {
                if (e is ANError)
                    Log.e(TAG, "upload-onError: ${e.response}")
            }
        })
    }

    fun onImgCreateClicked(view: View)
    {
        val picDialog = AlertDialog.Builder(this)
        val picDialogItems = arrayOf(resources.getString(R.string.from_gallery), resources.getString(R.string.take_photo))
        picDialog.setItems(picDialogItems)
        {
                _, which ->
            when (which)
            {
                0 -> choosePhotoFromGalley()
                1 -> takePhoto()
            }
        }
        picDialog.show()
    }

    private fun choosePhotoFromGalley()
    {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, 0)
    }

    private fun takePhoto()
    {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 1)
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0)//From gallery
        {
            if (data != null)
            {
                val contentUri = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                    profilePic!!.setImageBitmap(bitmap)
                    profilePicFile = Util.saveImage(cacheDir, bitmap)
                } catch (e: IOException){}
            }
        }
        else if (requestCode == 1)//Take photo
        {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            profilePic!!.setImageBitmap(thumbnail)
            profilePicFile = Util.saveImage(cacheDir, thumbnail)
        }
    }

    private fun requestRegister(userName: String, passWord: String, bio: String, isPrivate: Boolean, dialog: AlertDialog)
    {
        ApiService.register(userName, passWord, bio, isPrivate).subscribe(object: SingleObserver<TaskResponse>
        {
            override fun onSuccess(t: TaskResponse)
            {
                Log.d(TAG, "register-onSuccess: $t")

                dialog.dismiss()

                if (t.success)
                {
                    Log.i(TAG, "Registered successfully")

                    uploadProfilePic()
                }

                finish()
            }

            override fun onSubscribe(d: Disposable)
            {

            }

            override fun onError(e: Throwable)
            {
                dialog.dismiss()

                handleError(e)
            }
        })
    }

    private fun requestLogin(userName: String, passWord: String, dialog: AlertDialog)
    {
        ApiService.login(userName, passWord).subscribe(object: SingleObserver<LoginResponse>
        {
            override fun onSuccess(t: LoginResponse)
            {
                Log.d(TAG, "login-onSuccess: $t")

                dialog.dismiss()

                App.instance.prefs.setTokenAndUserName(t.token, userName)
                ApiService.token = t.token

                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }

            override fun onSubscribe(d: Disposable)
            {

            }

            override fun onError(e: Throwable)
            {
                dialog.dismiss()

                handleError(e)
            }
        })
    }

    private fun handleError(e: Throwable)
    {
        if (e is ANError)
            Log.e(TAG, "Error ${e.errorCode}, body:${e.errorBody}, details: ${e.errorDetail}")

        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onStop()
    {
        super.onStop()
        disposables.dispose()
    }

    private fun createProgressDialog(text: String): AlertDialog
    {
        val llPadding = 30
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(this)
        tvText.text = text
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        dialog.window?.let {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(it.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }

        return dialog
    }
}