package com.example.shediz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.shediz.model.Post

@Database(entities= [Post::class], version = 1)
@TypeConverters(value = [(DateConverter::class)])
abstract class PostDatabase: RoomDatabase()
{
    abstract fun postDao(): PostDao
}