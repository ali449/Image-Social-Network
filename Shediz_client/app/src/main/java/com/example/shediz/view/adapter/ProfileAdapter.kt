package com.example.shediz.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shediz.R
import com.example.shediz.data.network.ApiService
import com.example.shediz.model.Post
import com.example.shediz.model.User
import com.example.shediz.model.UserFollowMode
import com.example.shediz.view.helper.OnItemClickListener
import com.example.shediz.view.helper.OnViewClicked
import com.example.shediz.view.helper.PostThumbHolder
import kotlinx.android.synthetic.main.fragment_profile_header.view.*


class ProfileAdapter(private val context: Context, private val posts: ArrayList<Post>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    companion object
    {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private var user: User? = null

    private var onClickListener: OnItemClickListener? = null

    private var onFollowingClicked: OnViewClicked? = null

    private var onFollowersClicked: OnViewClicked? = null

    private var onProfileButtonClicked: OnViewClicked? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return when (viewType)
        {
            VIEW_TYPE_HEADER -> ProfileHeader(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_profile_header, parent, false))

            VIEW_TYPE_ITEM -> PostThumbHolder(context, LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post_grid, parent, false), onClickListener)

            else -> error("Unhandled viewType=$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        when (val viewType = getItemViewType(position))
        {
            VIEW_TYPE_HEADER -> (holder as ProfileHeader).bindView(user)
            VIEW_TYPE_ITEM -> (holder as PostThumbHolder).bindView(getItem(position).id, getItem(position).isSpam)
            else -> error("Unhandled viewType=$viewType")
        }
    }

    fun getItem(position: Int) = posts[position-1]

    override fun getItemCount(): Int = posts.size + 1

    override fun getItemViewType(position: Int) = when (position) {
        0 -> VIEW_TYPE_HEADER
        else -> VIEW_TYPE_ITEM
    }

    fun add(items: List<Post>)
    {
        val previousDataSize: Int = itemCount
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

        notifyItemRangeChanged(start+1, items.size)
    }

    fun setUser(user: User?)
    {
        this.user = user
        notifyItemChanged(0)
    }

    fun setUserFollowMode(followMode: UserFollowMode)
    {
        user!!.followMode = followMode
        notifyItemChanged(0)
    }

    fun getUser() = user

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener)
    {
        this.onClickListener = onItemClickListener
    }

    fun setOnFollowingClicked(onFollowingClicked: OnViewClicked)
    {
        this.onFollowingClicked = onFollowingClicked
    }

    fun setOnFollowersClicked(onFollowersClicked: OnViewClicked)
    {
        this.onFollowersClicked = onFollowersClicked
    }

    fun setOnProfileButtonClicked(onProfileButtonClicked: OnViewClicked)
    {
        this.onProfileButtonClicked = onProfileButtonClicked
    }

    inner class ProfileHeader(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        init
        {
            itemView.followingLayout.setOnClickListener { onFollowingClicked?.invoke(it) }
            itemView.followerLayout.setOnClickListener { onFollowersClicked?.invoke(it) }
            itemView.profileBtn.setOnClickListener { onProfileButtonClicked?.invoke(it) }
        }

        internal fun bindView(user: User?)
        {
            if (user == null)
                return

            itemView.userNameTxt.text = user.userName
            itemView.bioTxt.text = user.bio
            itemView.followingTxt.text = user.numFollowing.toString()
            itemView.followerTxt.text = user.numFollowers.toString()

            when (user.followMode)
            {
                UserFollowMode.SELF ->
                {
                    itemView.profileBtn.text = context.resources.getString(R.string.edit_profile)
                    itemView.profileBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_edit, 0, 0, 0)
                }
                UserFollowMode.FOLLOW ->
                {
                    itemView.profileBtn.text = context.resources.getString(R.string.follow)
                    itemView.profileBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_follow, 0, 0, 0)
                }
                UserFollowMode.REQUEST_FOLLOW ->
                {
                    itemView.profileBtn.text = context.resources.getString(R.string.request_follow)
                    itemView.profileBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_lock, 0, 0, 0)
                }
                UserFollowMode.UN_FOLLOW ->
                {
                    itemView.profileBtn.text = context.resources.getString(R.string.un_follow)
                    itemView.profileBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_unfollow, 0, 0, 0)
                }
            }

            ApiService.loadProfileImage(context, user.userName).into(itemView.profileImg)
        }
    }
}