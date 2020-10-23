package com.example.shediz.model

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Like(val postId: String, val numLikes: Long, val isUserLiked: Boolean)

data class Comment(@PrimaryKey val cid: Long, @SerializedName("username") val userName: String,
                   val postId: String, val text: String)