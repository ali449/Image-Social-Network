package com.example.shediz.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.shediz.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object Util
{
    fun copyTextToClipboard(context: Context, text: String)
    {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }

    fun shareImage(context: Context, url: String)
    {
        Glide.with(context).asFile().load(url).listener(object : RequestListener<File>
        {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File>?,
                                      isFirstResource: Boolean): Boolean = true

            override fun onResourceReady(resource: File?, model: Any?, target: Target<File>?, dataSource: DataSource?,
                                         isFirstResource: Boolean): Boolean
            {
                val path = resource!!.path

                val intentShareFile = Intent(Intent.ACTION_SEND)
                val fileWithinMyDir = File(path)

                if (fileWithinMyDir.exists())
                {
                    intentShareFile.type = "image/jpeg"
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$path"))
                    context.startActivity(Intent.createChooser(intentShareFile,
                        context.resources.getString(R.string.share_image)))
                }

                return true
            }
        }).submit()
    }

    fun saveImage(cacheDir: File, bitmap: Bitmap): File?
    {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes)
        val file = File(cacheDir, System.currentTimeMillis().toString() + ".jpg")
        try
        {
            val fo = FileOutputStream(file)
            fo.write(bytes.toByteArray())
            //MediaScannerConnection.scanFile(this, arrayOf(file.path), arrayOf("image/jpeg"), null)
            fo.flush()
            fo.close()
            return file
        } catch (e: IOException)
        {
        }

        return null
    }

    //Cannot called in main thread
    fun canAccessInternet(context: Context): Boolean
    {
        if (isNetworkAvailable(context))
        {
            try
            {
                val urlc = URL("http://www.google.com").openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "Test")
                urlc.setRequestProperty("Connection", "close")
                urlc.connectTimeout = 1500
                urlc.connect()
                return urlc.responseCode == 200
            } catch (e: IOException) { }
        }
        return false
    }

    fun isNetworkAvailable(context: Context): Boolean
    {
        var isAvailable = false


        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        val networkInfo = manager?.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected)
        {
            isAvailable = true
        }

        /*
            ConnectionQuality connectionQuality = AndroidNetworking.getCurrentConnectionQuality();
            if(connectionQuality == ConnectionQuality.EXCELLENT) {
              // do something
            } else if (connectionQuality == ConnectionQuality.POOR) {
              // do something
            } else if (connectionQuality == ConnectionQuality.UNKNOWN) {
              // do something
            }
            // Getting current bandwidth
            int currentBandwidth = AndroidNetworking.getCurrentBandwidth();// Note : if (currentBandwidth == 0) : means UNKNOWN
         */

        return isAvailable
    }
}