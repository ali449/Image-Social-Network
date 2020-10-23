package com.example.shediz.view.helper

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.shediz.App
import com.example.shediz.R
import com.example.shediz.data.db.DateConverter
import com.example.shediz.data.network.ApiService
import com.example.shediz.model.Post
import kotlinx.android.synthetic.main.item_post_full.view.*


class PostHolder(private val context: Context, itemView: View,
                 private val onListActionListener: OnListActionListener?): RecyclerView.ViewHolder(itemView)
{
    init
    {
        itemView.postOwnerPic.setOnClickListener { onListActionListener?.onUserClickListener(adapterPosition, it) }
        itemView.postOwnerName.setOnClickListener { onListActionListener?.onUserClickListener(adapterPosition, it) }
        itemView.optionsBtn.setOnClickListener { onListActionListener?.onOptionsClickListener(adapterPosition, it) }
        itemView.likeBtn.setOnClickListener { onListActionListener?.onLikeClickListener(adapterPosition, it) }
        itemView.commentBtn.setOnClickListener { onListActionListener?.onCommentClickListener(adapterPosition, it) }
        itemView.shareBtn.setOnClickListener { onListActionListener?.onShareClickListener(adapterPosition, it) }
    }

    internal fun bindView(post: Post)
    {
        itemView.postOwnerName.text = post.userName
        itemView.postDate.text = DateConverter.fromDate(post.date)
        itemView.contentText.text = post.content
        itemView.numLikesText.text = App.instance.resources.getString(R.string.num_likes, (post.numLikes ?: 0).toString())

        if (post.isUserLiked != null)
        {
            if (post.isUserLiked!!)
                itemView.likeBtn.setImageResource(R.drawable.ic_like_filled)
            else
                itemView.likeBtn.setImageResource(R.drawable.ic_like)
        }

        //Access to data from view is an anti pattern!

        ApiService.loadProfileImage(context, post.userName).into(itemView.postOwnerPic)
        ApiService.loadFullPostImage(context, post.id).into(itemView.postImage)
    }
}