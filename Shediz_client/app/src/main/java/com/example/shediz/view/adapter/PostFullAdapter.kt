package com.example.shediz.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shediz.R
import com.example.shediz.model.Post
import com.example.shediz.view.helper.OnListActionListener
import com.example.shediz.view.helper.PostHolder


class PostFullAdapter(private val context: Context, private val posts: ArrayList<Post>)
    : RecyclerView.Adapter<PostHolder>()
{
    private var onListActionListener: OnListActionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PostHolder(context, LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post_full, parent, false), onListActionListener)

    override fun onBindViewHolder(holder: PostHolder, position: Int)
    {
        holder.bindView(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun getItem(position: Int): Post = posts[position]

    fun add(items: List<Post>)
    {
        val previousDataSize: Int = posts.size
        posts.addAll(items)
        notifyItemRangeInserted(previousDataSize, itemCount)
    }

    fun clear()
    {
        posts.clear()
        notifyDataSetChanged()
    }

    fun removeItemById(postId: String)
    {
        val post = posts.withIndex().find { it.value.id == postId }

        if (post != null)
        {
            posts.removeAt(post.index)
            notifyItemRemoved(post.index)
        }
    }

    fun setPostLikeChanged(postId: String, isUserLiked: Boolean)
    {
        val post = posts.withIndex().find { it.value.id == postId }

        if (post != null)
        {
            post.value.isUserLiked = isUserLiked

            var numLikes =  post.value.numLikes ?: 0
            if (isUserLiked)
                numLikes++
            else
                numLikes--
            post.value.numLikes = numLikes

            posts[post.index] = post.value
            notifyItemChanged(post.index)
        }
    }

    fun setInRange(items: List<Post>, start: Int, end: Int)
    {
        if (posts.size < items.size)
            posts.addAll(items)
        else
        {
            var j = 0
            for (i in start until end)
                posts[i] = items[j++]
        }

        notifyItemRangeChanged(start, items.size)
    }

    fun setListener(onListActionListener: OnListActionListener)
    {
        this.onListActionListener = onListActionListener
    }
}