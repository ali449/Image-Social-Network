package com.example.shediz.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "Post")
data class Post(@PrimaryKey val id: String, @SerializedName("username") val userName: String, val content: String,
                val date: Date, val isSpam: Boolean, var numLikes: Long?, var isUserLiked: Boolean?, var type: Int?)