package com.example.shediz.data.network

import com.androidnetworking.error.ANError
import com.example.shediz.utils.Constants
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.io.File

/*
    We use OkHttp to upload the image,
    because FastAndroidNetworking adds an additional header (Content_Length)
    to the request and we can't remove it.
    Getting unexpected end of stream error.
 */

class ImageUploader(private val prefixAuth: String, private val token: String)
{
    fun uploadPost(file: File, content: String): Single<CreateResponse>
    {
        val req = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("content", null, RequestBody
                .create(MediaType.parse("text/plain"), content))
            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("Image/JPEG"), file))
            .build()

        val request = Request.Builder()
            .header(prefixAuth, token)
            .url("${Constants.BASE_URL}/post")
            .post(req)
            .build()

        val client = OkHttpClient()

        return Single.create<CreateResponse> {
            try
            {
                val response = client.newCall(request!!)?.execute()
                if (!it.isDisposed)
                {
                    if (response!!.isSuccessful)
                        it.onSuccess(Gson().fromJson(response.body()!!.charStream(), CreateResponse::class.java))
                    else
                        it.onError(ANError(response))
                }
            }
            catch (e: java.lang.Exception)
            {
                it.onError(ANError(e))
            }

        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}