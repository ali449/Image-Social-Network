package com.example.shediz.view.helper

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.shediz.data.network.ApiService
import kotlinx.android.synthetic.main.item_post_grid.view.*

class PostThumbHolder(private val context: Context, itemView: View,
                      private val onItemClickListener: OnItemClickListener?): RecyclerView.ViewHolder(itemView)
{
    init
    {
        itemView.setOnClickListener { onItemClickListener?.invoke(adapterPosition, it) }
    }

    internal fun bindView(postId: String, isAdvertise: Boolean)
    {
        ApiService.loadThumbPostImage(context, postId).into(itemView.postImageThumb)

        itemView.advertiseImg.visibility = if (isAdvertise) View.VISIBLE else View.GONE
    }
}