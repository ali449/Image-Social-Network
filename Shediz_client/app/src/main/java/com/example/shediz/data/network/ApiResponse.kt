package com.example.shediz.data.network

import com.google.gson.annotations.SerializedName


data class LoginResponse(val token: String)

data class TaskResponse(val success: Boolean)

data class CommentPutResponse(val cid: Long)

enum class TaskType
{
    USER_FOLLOW, USER_UNFOLLOW, USER_FOLLOW_REQUEST,
    POST_REMOVED, POST_LIKED, POST_UNLIKED, COMMENT_CREATED, COMMENT_REMOVED
}
data class TaskResult(val success: Boolean, val username: String?, val pid: String?, val cid: Long?,
                      val type: TaskType)

data class UserWithFollow(@SerializedName("username") val userName: String, val isPrivate: Boolean,
                          val bio: String?, val numFollowing: Int, val numFollowers: Int, val isEnabled: Boolean,
                          val isFollowing: Boolean)

data class CreateResponse(@SerializedName("save_id") val saveId: String)