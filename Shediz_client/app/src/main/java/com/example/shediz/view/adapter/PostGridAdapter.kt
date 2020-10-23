package com.example.shediz.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shediz.R
import com.example.shediz.model.Post
import com.example.shediz.view.helper.OnItemClickListener
import com.example.shediz.view.helper.PostThumbHolder


class PostGridAdapter(private val context: Context, private val posts: ArrayList<Post>)
    : RecyclerView.Adapter<PostThumbHolder>()
{
    private var onClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PostThumbHolder(context, LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_grid, parent, false), onClickListener)

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: PostThumbHolder, position: Int)
    {
        holder.bindView(posts[position].id, posts[position].isSpam)
    }

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

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener)
    {
        this.onClickListener = onItemClickListener
    }
}