package com.example.shediz.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shediz.R
import com.example.shediz.data.network.ApiService
import com.example.shediz.model.User
import com.example.shediz.view.helper.OnItemClickListener
import kotlinx.android.synthetic.main.item_user.view.*

class UserAdapter(private val context: Context, private val users: ArrayList<User>)
    : RecyclerView.Adapter<UserAdapter.UserHolder>()
{
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UserHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserHolder, position: Int)
    {
        holder.bindView(users[position])
    }

    fun getItem(position: Int) = users[position]

    fun add(items: List<User>)
    {
        val previousDataSize: Int = users.size
        users.addAll(items)
        notifyItemRangeInserted(previousDataSize, itemCount)
    }

    fun clear()
    {
        users.clear()
        notifyDataSetChanged()
    }

    fun setInRange(items: List<User>, start: Int, end: Int)
    {
        if (users.size < items.size)
            users.addAll(items)
        else
        {
            var j = 0
            for (i in start until end)
                users[i] = items[j++]
        }

        notifyItemRangeChanged(start, items.size)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener)
    {
        this.onItemClickListener = onItemClickListener
    }

    inner class UserHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        init
        {
            itemView.setOnClickListener { onItemClickListener?.invoke(adapterPosition, it) }
        }

        internal fun bindView(user: User)
        {
            itemView.userNameItem.text = user.userName
            itemView.userBioItem.text = user.bio

            ApiService.loadProfileImage(context, user.userName).into(itemView.userImg)
        }
    }
}
