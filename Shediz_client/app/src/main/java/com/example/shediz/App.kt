package com.example.shediz

import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.example.shediz.data.db.PostDatabase
import com.example.shediz.utils.SharedPref
import com.google.gson.GsonBuilder

class App: MultiDexApplication()
{
    companion object
    {
        lateinit var instance: App
            private set
    }

    lateinit var prefs: SharedPref

    lateinit var db: PostDatabase

    override fun onCreate()
    {
        super.onCreate()

        instance = this

        prefs = SharedPref(applicationContext)

        db = Room.databaseBuilder(applicationContext, PostDatabase::class.java, "shediz.db").build()

        AndroidNetworking.initialize(applicationContext)
        //AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY)
        val gson = GsonBuilder().setLenient().setDateFormat("yyyy-MM-dd").create()
        AndroidNetworking.setParserFactory(GsonParserFactory(gson))
    }
}