package com.example.shediz.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shediz.R
import com.example.shediz.data.network.ApiService
import com.example.shediz.model.Comment
import com.example.shediz.view.helper.OnItemClickListener
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentAdapter(private val context: Context, private val comments: ArrayList<Comment>)
    : RecyclerView.Adapter<CommentAdapter.CommentHolder>()
{
    private var onClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CommentHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false))

    override fun getItemCount() = comments.size

    override fun onBindViewHolder(holder: CommentHolder, position: Int)
    {
        holder.bindView(comments[position])
    }

    fun getItem(position: Int) = comments[position]

    fun add(comment: Comment)
    {
        comments.add(comment)
        notifyItemInserted(itemCount)
    }

    fun deleteByCid(cid: Long)
    {
        val comment = comments.withIndex().find { it.value.cid == cid }

        if (comment != null)
        {
            comments.removeAt(comment.index)
            notifyItemRemoved(comment.index)
        }
    }

    fun addList(items: List<Comment>)
    {
        val previousDataSize: Int = comments.size
        comments.addAll(items)
        notifyItemRangeInserted(previousDataSize, itemCount)
    }

    fun clear()
    {
        comments.clear()
        notifyDataSetChanged()
    }

    fun setInRange(items: List<Comment>, start: Int, end: Int)
    {
        if (comments.size < items.size)
            comments.addAll(items)
        else
        {
            var j = 0
            for (i in start until end)
                comments[i] = items[j++]
        }

        notifyItemRangeChanged(start, items.size)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener)
    {
        this.onClickListener = onItemClickListener
    }

    inner class CommentHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        init
        {
            itemView.setOnClickListener { onClickListener?.invoke(adapterPosition, it) }
        }

        fun bindView(comment: Comment)
        {
            itemView.userNameComment.text = comment.userName
            itemView.commentTxt.text = comment.text

            ApiService.loadProfileImage(context, comment.userName).into(itemView.commentUserImg)
        }
    }
}