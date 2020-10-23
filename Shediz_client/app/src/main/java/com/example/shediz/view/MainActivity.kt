package com.example.shediz.view

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.shediz.App
import com.example.shediz.R
import com.example.shediz.messaging.ReceiverService
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    private var receiverService: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUi()

        backBtn.setOnClickListener { onBackPressed() }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0)
            {
                backBtn.visibility = View.GONE
                computeTitle()
            }
            else
            {
                backBtn.visibility = View.VISIBLE

                val f = supportFragmentManager.findFragmentById(R.id.container)
                if (f != null && f is SinglePostFragment)
                    f.updateTitle()
            }
        }

        receiverService = Intent(this, ReceiverService::class.java)
        startService(receiverService)

        if (savedInstanceState == null)
        {
            val fragment = HomeFragment()
            supportFragmentManager
                .beginTransaction().replace(R.id.container, fragment, fragment::class.simpleName)
                .commit()
        }
    }

    override fun onDestroy()
    {
        if (receiverService != null)
            stopService(receiverService)

        super.onDestroy()
    }

    override fun onBackPressed()
    {
        if (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStack()
        else
            super.onBackPressed()
    }

    //For call in fragments
    fun setToolbarTitle(title: String)
    {
        toolbar_title.text = title
    }

    private fun initUi()
    {
        toolbarMain.title = resources.getString(R.string.app_name)
        setSupportActionBar(toolbarMain)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //Increase BottomNavigationView icon size
        var i = 0
        val navigationMenuViews = bottomMenu.getChildAt(0) as BottomNavigationMenuView
        while (i < navigationMenuViews.childCount)
        {
            val iconView = navigationMenuViews.getChildAt(i).findViewById(com.google.android.material.R.id.icon) as ImageView
            val layoutParams = iconView.layoutParams
            layoutParams.height = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30F, resources.displayMetrics).toInt()
            layoutParams.width = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30F, resources.displayMetrics).toInt()
            iconView.layoutParams = layoutParams
            i++
        }

        bottomMenu.setOnNavigationItemReselectedListener { }

        bottomMenu.setOnNavigationItemSelectedListener {
            when (it.itemId)
            {
                R.id.menuItem1 ->
                {
                    toolbar_title.text = resources.getString(R.string.app_name)

                    val fragment = HomeFragment()
                    supportFragmentManager
                        .beginTransaction().replace(R.id.container, fragment, fragment::class.simpleName)
                        .commit()
                }
                R.id.menuItem2 ->
                {
                    toolbar_title.text = resources.getString(R.string.recommended)

                    val fragment = RecFragment()
                    supportFragmentManager
                        .beginTransaction().replace(R.id.container, fragment, fragment::class.simpleName)
                        .commit()
                }
                R.id.menuItem3 ->
                {
                    toolbar_title.text = resources.getString(R.string.search)

                    val fragment = SearchFragment()
                    supportFragmentManager
                        .beginTransaction().replace(R.id.container, fragment, fragment::class.simpleName)
                        .commit()
                }
                R.id.menuItem4 ->
                {
                    toolbar_title.text = resources.getString(R.string.create_post)

                    val fragment = CreateFragment()
                    supportFragmentManager
                        .beginTransaction().replace(R.id.container, fragment, fragment::class.simpleName)
                        .commit()
                }
                R.id.menuItem5 ->
                {
                    toolbar_title.text = resources.getString(R.string.profile)

                    val fragment = ProfileFragment(App.instance.prefs.getUserName()!!)
                    supportFragmentManager
                        .beginTransaction().replace(R.id.container, fragment, fragment::class.simpleName)
                        .commit()
                }
            }
            true
        }
    }

    private fun computeTitle()
    {
        val selectedItem = bottomMenu.menu.findItem(bottomMenu.selectedItemId)
        when (selectedItem.itemId)
        {
            R.id.menuItem1 -> toolbar_title.text = resources.getString(R.string.app_name)
            R.id.menuItem2 -> toolbar_title.text = resources.getString(R.string.recommended)
            R.id.menuItem3 -> toolbar_title.text = resources.getString(R.string.search)
            R.id.menuItem4 -> toolbar_title.text = resources.getString(R.string.create_post)
            R.id.menuItem5 -> toolbar_title.text = resources.getString(R.string.profile)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.menu_setting -> startActivity(Intent(this, SettingActivity::class.java))
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}