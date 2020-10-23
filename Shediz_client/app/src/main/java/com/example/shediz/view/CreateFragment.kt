package com.example.shediz.view

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.androidnetworking.error.ANError
import com.example.shediz.R
import com.example.shediz.data.network.ApiService
import com.example.shediz.data.network.CreateResponse
import com.example.shediz.utils.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_create.*
import java.io.File
import java.io.IOException


class CreateFragment : Fragment()
{
    companion object
    {
        private val TAG = "TAG_" + CreateFragment::class.simpleName
    }

    private var postPicFile: File? = null

    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_create, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        createImg.setOnClickListener {
            val picDialog = AlertDialog.Builder(requireContext())
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

        postBtn.setOnClickListener {
            if (postPicFile == null)
                return@setOnClickListener

            progressCreate.visibility = View.VISIBLE
            ApiService.createPost(postPicFile!!, inputPost.text.toString()).subscribe(object : SingleObserver<CreateResponse>
            {
                override fun onSuccess(t: CreateResponse)
                {
                    progressCreate.visibility = View.GONE

                    Toast.makeText(requireContext(), "Successfully!", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "saveId: ${t.saveId}")

                    createImg.setImageResource(R.drawable.placeholder_fallback)
                    inputPost.text.clear()
                }

                override fun onSubscribe(d: Disposable)
                {
                    disposables.addAll(d)
                }

                override fun onError(e: Throwable)
                {
                    progressCreate.visibility = View.GONE

                    if (e is ANError)
                        Log.e(TAG, "Error ${e.errorCode}, Body: ${e.errorBody}, Details: ${e.errorDetail}")
                }
            })
        }
    }

    override fun onStop()
    {
        super.onStop()
        Log.i(TAG, "onStop()")
        disposables.clear()
    }

    private fun choosePhotoFromGalley()
    {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null)
            startActivityForResult(intent, 0)
    }

    private fun takePhoto()
    {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0)//From gallery
        {
            if (data != null)
            {
                val contentUri = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, contentUri)
                    createImg!!.setImageBitmap(bitmap)

                    postPicFile = Util.saveImage(requireActivity().cacheDir, bitmap)
                } catch (e: IOException){}
            }
        }
        else if (requestCode == 1)//Take photo
        {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            createImg!!.setImageBitmap(thumbnail)
            postPicFile = Util.saveImage(requireActivity().cacheDir, thumbnail)
        }
    }
}