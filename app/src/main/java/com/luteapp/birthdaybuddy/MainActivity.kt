package com.luteapp.birthdaybuddy

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import com.luteapp.birthdaybuddy.fragments.*
import com.luteapp.birthdaybuddy.handler.BitmapHandler
import com.luteapp.birthdaybuddy.handler.EventHandler
import com.luteapp.birthdaybuddy.handler.IOHandler
import com.luteapp.birthdaybuddy.models.AnnualEvent
import com.luteapp.birthdaybuddy.models.EventBirthday
import com.luteapp.birthdaybuddy.models.MonthDivider
import com.luteapp.birthdaybuddy.models.OneTimeEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_event_list.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        EventHandler.clearData()

        IOHandler.registerIO(this)

        lockAppbar()

        if (!IOHandler.isFirstStart()) {
            //read all data from shared prefs, when app didnt start for the first time
            IOHandler.readAll(this)

        } else {
            //on first start write standard settings to shared prefs
            IOHandler.initializeAllSettings()

            addMonthDivider()
        }


        //set default night module
        val useDarkMode = IOHandler.getBooleanFromKey(IOHandler.SharedPrefKeys.key_use_dark_mode)
        if (useDarkMode == null) {
            //if dark mode variable was not set before, set it now
            IOHandler.writeSetting(IOHandler.SharedPrefKeys.key_use_dark_mode, false)
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        } else {
            if (useDarkMode) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
        }

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(
                R.id.fragment_placeholder,
                EventListFragment.newInstance()
            ).commit()
        }

        //start loading bitmap drawables in other thread to not block ui
        Thread(Runnable
        {
            BitmapHandler.loadAllBitmaps(this)
            runOnUiThread {
                if (recyclerView != null) {
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
            }
        }).start()

        if (intent != null) {
            if (intent?.getBooleanExtra(FRAGMENT_EXTRA_TITLE_LOADALL, false) == true) {
                val eventID = intent?.getIntExtra(FRAGMENT_EXTRA_TITLE_EVENTID, -1)
                val type = intent?.getStringExtra(FRAGMENT_EXTRA_TITLE_TYPE)
                if (eventID != null && eventID > -1 && type != null) {
                    startFragments(eventID, type)
                }
            }
            intent = null
        }
    }

    fun unlockAppBar() {
        app_bar.isActivated = true
        setAppBarDragging(true)
    }

    fun lockAppbar() {
        this.app_bar.setExpanded(false, false)
        app_bar.isActivated = false
        setAppBarDragging(false)
    }

    private fun setAppBarDragging(isEnabled: Boolean) {
        val params = this.app_bar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return isEnabled
            }
        })
        params.behavior = behavior
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val eventID = intent?.getIntExtra(FRAGMENT_EXTRA_TITLE_EVENTID, -1)
        val type = intent?.getStringExtra(FRAGMENT_EXTRA_TITLE_TYPE)
        if (toolbar.menu.findItem(R.id.toolbar_search)?.actionView != null) {

            (toolbar.menu.findItem(R.id.toolbar_search)?.actionView as androidx.appcompat.widget.SearchView).apply {
                //close search view
                toolbar.collapseActionView()
            }

            if (eventID != null && eventID > -1 && type != null) {
                startFragments(eventID, type)
            }
        }
    }

    private fun startFragments(eventID: Int, type: String) {
        val bundle = Bundle()
        //do this in more adaptable way
        bundle.putInt(
            FRAGMENT_EXTRA_TITLE_EVENTID,
            eventID
        )

        EventHandler.getEventToEventIndex(eventID)?.let { event ->

            val eventFragment: Fragment? = when (event) {
                is EventBirthday -> {
                    if (type == FRAGMENT_TYPE_SHOW) {
                        ShowBirthdayEvent.newInstance()
                    } else {
                        BirthdayInstanceFragment.newInstance()
                    }
                }
                is AnnualEvent -> {
                    if (type == FRAGMENT_TYPE_SHOW) {
                        ShowAnnualEvent.newInstance()
                    } else {
                        AnnualEventInstanceFragment.newInstance()
                    }
                }
                is OneTimeEvent -> {
                    if (type == FRAGMENT_TYPE_SHOW) {
                        ShowOneTimeEvent.newInstance()
                    } else {
                        OneTimeEventInstanceFragment.newInstance()
                    }
                }
                else -> {
                    null
                }
            }
            if (eventFragment != null) {
                val ft = supportFragmentManager.beginTransaction()
                // add arguments to fragment
                eventFragment.arguments = bundle
                ft.replace(
                    R.id.fragment_placeholder,
                    eventFragment
                )
                ft.addToBackStack(null)
                ft.commit()
            }
        }
    }

    /**
     * addMonthDivider adds all 12 month dividers (dividers between events to group them in month groups)
     */
    fun addMonthDivider() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        for (i in 0 until 12) {
            cal.set(Calendar.MONTH, i)
            EventHandler.addEvent(
                MonthDivider(cal.time, resources.getStringArray(R.array.month_names)[i]),
                this,
                true
            )
        }
    }

    /**
     * onRequestPermissionsResult is the callback function after requesting the users permission for android permissions
     * In this case we request READ/WRITE rights on external storage and handle exporting/ importing event data from the external storage
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            //writing to external
            6001 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    writeDataToExternal()
                } else {
                    Toast.makeText(
                        this,
                        R.string.permissions_toast_denied_write,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            //reading from external
            6002 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    importDataFromExternal()
                } else {
                    Toast.makeText(
                        this,
                        R.string.permissions_toast_denied_read,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun writeDataToExternal() {
        if (IOHandler.writeAllEventsToExternalStorage(this)) {

            try {
                this.supportFragmentManager.popBackStack()
            } catch (e: Exception) {
                Log.e("MainActivity", e.localizedMessage)
            }

            Snackbar.make(
                main_coordinator_layout,
                R.string.permissions_snackbar_granted_write,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    fun importDataFromExternal() {
        if (IOHandler.importEventsFromExternalStorage(this)) {

            try {
                this.supportFragmentManager.popBackStack()
            } catch (e: Exception) {
                Log.e("MainActivity", e.localizedMessage)
            }

            Snackbar.make(
                main_coordinator_layout,
                R.string.permissions_snackbar_granted_read,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        fun convertPxToDp(context: Context, px: Float): Float {
            val metrics = context.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, metrics)
        }

        fun convertDpToPx(context: Context, dp: Float): Int {
            val metrics = context.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
        }

        const val FRAGMENT_TYPE_SHOW = "SHOW"
        const val FRAGMENT_TYPE_EDIT = "EDIT"

        const val FRAGMENT_EXTRA_TITLE_TYPE = "TYPE"
        const val FRAGMENT_EXTRA_TITLE_EVENTID = "EVENTID"
        const val FRAGMENT_EXTRA_TITLE_EVENTSTRING = "EVENTSTRING"
        const val FRAGMENT_EXTRA_TITLE_NOTIFICATIONID = "NOTIFICATIONID"
        const val FRAGMENT_EXTRA_TITLE_POSITION = "POSITION"
        const val FRAGMENT_EXTRA_TITLE_LOADALL = "LOADALL"
    }
}
