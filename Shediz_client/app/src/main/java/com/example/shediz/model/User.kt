package com.example.shediz.model

import com.google.gson.annotations.SerializedName

enum class UserFollowMode
{
    SELF,//this is logged in user
    FOLLOW,//request follow to public user
    UN_FOLLOW,//request un follow user
    REQUEST_FOLLOW//request follow to private user
}

data class User(@SerializedName("username") val userName: String, val isPrivate: Boolean, val bio: String?,
                val numFollowing: Int?, val numFollowers: Int?, val isEnabled: Boolean, var followMode: UserFollowMode?)