package com.example.shediz.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.shediz.R
import com.example.shediz.model.User

class SharedPref(private val context: Context)
{
    private val PRIVATE_MODE = 0
    private val PREF_NAME = "user_info"

    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

    /*fun setUser(user: User)
    {
        val editor = sharedPref.edit()
        editor.putString("username", user.userName)
        editor.putBoolean("is_private", user.isPrivate)
        editor.putString("bio", user.bio)
        editor.putInt("num_following", user.numFollowing ?: 0)
        editor.putInt("num_followers", user.numFollowers ?: 0)
        editor.apply()
    }

    fun getUser(): User = User(getUserName()!!, getIsPrivate(), getUserBio(), getNumFollowing(), getNumFollowers(),
        true)  */

    fun setTokenAndUserName(token: String, userName: String)
    {
        val editor = sharedPref.edit()
        editor.putString("token", token)
        editor.putString("username", userName)
        editor.apply()
    }

    fun getUserName() = sharedPref.getString("username", context.resources.getString(R.string.username))

    /*fun getIsPrivate() = sharedPref.getBoolean("is_private", false)

    fun getUserBio() = sharedPref.getString("bio", context.resources.getString(R.string.bio))

    fun getNumFollowing() = sharedPref.getInt("num_following", 0)

    fun getNumFollowers() = sharedPref.getInt("num_followers", 0)*/

    fun getUserToken() = sharedPref.getString("token", "")
}