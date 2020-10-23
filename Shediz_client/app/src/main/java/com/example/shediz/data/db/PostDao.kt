package com.example.shediz.data.db

import androidx.room.*
import com.example.shediz.model.Post

@Dao
interface PostDao
{
    /*
        Different types:
        0 -> feed (new posts)
        1 -> current user posts
        2 -> recommended results
     */

    @Transaction
    @Query("SELECT * FROM Post WHERE type=2 LIMIT :offset, :size")
    fun loadLimitedRecPosts(size: Int, offset: Int): List<Post>

    @Transaction
    @Query("SELECT * FROM Post WHERE type=1 LIMIT :offset, :size")
    fun loadLimitedUserPosts(size: Int, offset: Int): List<Post>

    @Transaction
    @Query("SELECT * FROM Post WHERE type=0 LIMIT :offset, :size")
    fun loadLimitedFeed(size: Int, offset: Int): List<Post>

    @Transaction
    @Query("SELECT * FROM Post WHERE type=0")
    fun loadAllFeed(): List<Post>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPosts(posts: List<Post>)

    @Transaction
    @Query("DELETE FROM Post WHERE type=0")
    fun deleteFeed()

    @Transaction
    @Query("DELETE FROM Post WHERE id=:pid")
    fun deletePost(pid: String)

    @Transaction
    @Query("UPDATE Post SET isUserLiked=:likeStatus WHERE id=:pid")
    fun updateLikeStatus(pid: String, likeStatus: Boolean)
}