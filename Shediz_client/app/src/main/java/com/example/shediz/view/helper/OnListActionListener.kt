package com.example.shediz.view.helper

import android.view.View


interface OnListActionListener
{
    fun onUserClickListener(position: Int, view: View)

    fun onLikeClickListener(position: Int, view: View)

    fun onCommentClickListener(position: Int, view: View)

    fun onShareClickListener(position: Int, view: View)

    fun onOptionsClickListener(position: Int, view: View)
}