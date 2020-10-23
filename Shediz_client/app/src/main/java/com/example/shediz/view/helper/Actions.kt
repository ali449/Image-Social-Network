package com.example.shediz.view.helper

import android.content.Context
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.shediz.App
import com.example.shediz.R
import com.example.shediz.model.Post
import com.example.shediz.utils.Constants
import com.example.shediz.utils.Util
import com.example.shediz.view.CommentFragment
import com.example.shediz.view.FollowFragment
import com.example.shediz.view.ProfileFragment
import com.example.shediz.view.SinglePostFragment

class Actions(private val fragmentRoot: Fragment, private val fragmentManager: FragmentManager)
{
    fun openFollowFragment(userName: String, isFollowersMode: Boolean)
    {
        val fragment = FollowFragment(userName, isFollowersMode)
        val containerId = (fragmentRoot.requireView().parent as ViewGroup).id
        if (!fragment.isAdded)
        {
            fragmentManager
                .beginTransaction()
                .hide(fragmentRoot)
                .add(containerId, fragment, fragment::class.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    fun openSinglePost(post: Post)
    {
        val fragment = SinglePostFragment(post)
        val containerId = (fragmentRoot.requireView().parent as ViewGroup).id
        if (!fragment.isAdded)
        {
            fragmentManager
                .beginTransaction()
                .hide(fragmentRoot)
                .add(containerId, fragment, fragment::class.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    fun openProfileFragment(userName: String)
    {
        val containerId = (fragmentRoot.requireView().parent as ViewGroup).id
        val fragment = ProfileFragment(userName)

        if (!fragment.isAdded)
        {
            fragmentManager
                .beginTransaction()
                .hide(fragmentRoot)
                .add(containerId, fragment, fragment::class.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    fun openCommentFragment(postId: String)
    {
        val containerId = (fragmentRoot.requireView().parent as ViewGroup).id
        val fragment = CommentFragment(postId)

        if (!fragment.isAdded)
        {
            fragmentManager
                .beginTransaction()
                .hide(fragmentRoot)
                .add(containerId, fragment, fragment::class.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    fun openOptionsDialog(post: Post, onDeleteClicked: (String) -> Unit)
    {
        val context = fragmentRoot.requireContext()

        val optionsDialog = AlertDialog.Builder(context)

        var dialogItems = arrayOf(context.resources.getString(R.string.copy_text),
            context.resources.getString(R.string.copy_link),
            context.resources.getString(R.string.share_image))

        val loggedInUserName = App.instance.prefs.getUserName()
        if (loggedInUserName == post.userName)
            dialogItems += context.resources.getString(R.string.delete_post)

        optionsDialog.setItems(dialogItems) { dialog, which ->
            dialog.dismiss()
            when (which)
            {
                0 -> Util.copyTextToClipboard(context, post.content)
                1 -> Util.copyTextToClipboard(context, "${Constants.BASE_URL}/post/s/main_image/${post.id}")
                2 -> Util.shareImage(context, "${Constants.BASE_URL}/post/main_image/${post.id}")
                3 -> askDeletePost(context, post.id, onDeleteClicked)
            }
        }
        optionsDialog.show()
    }

    fun shareImage(postId: String)
    {
        Util.shareImage(fragmentRoot.requireContext(), "${Constants.BASE_URL}/post/main_image/${postId}")
    }

    private fun askDeletePost(context: Context, postId: String, onDeleteClicked: (String) -> Unit)
    {
        val mAlertDialog = AlertDialog.Builder(context)
        mAlertDialog.setIcon(R.mipmap.ic_launcher_round)
        mAlertDialog.setTitle(context.resources.getString(R.string.delete_confirm))
        mAlertDialog.setMessage(context.resources.getString(R.string.are_you_sure))
        mAlertDialog.setPositiveButton(context.resources.getString(R.string.yes)) { dialog, _ ->
            onDeleteClicked.invoke(postId)
            dialog.dismiss()
        }
        mAlertDialog.setNegativeButton(context.resources.getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        mAlertDialog.show()
    }
}