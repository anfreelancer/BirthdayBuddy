package com.luteapp.birthdaybuddy

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import android.widget.TextView
import com.luteapp.birthdaybuddy.handler.SearchHandler
import com.luteapp.birthdaybuddy.views.EventAdapterSearching
import com.luteapp.birthdaybuddy.views.RecycleViewItemDivider
import kotlinx.android.synthetic.main.activity_searchable.*

/**
 * SearchableActivity is the activity used for searching events by their name
 * This has to be an extra activity because of the android framework functionality of the search view
 *
 * After a search has been completed by the user (clicking the magnifying glass in the search view) a query string is send to this activity which
 * returns all events which containt the query string in their names. After retrieving those events, they are displayed in the recyclerview
 */
class SearchableActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var eventIndexList = emptyList<Int>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        setSupportActionBar(toolbar_searchable)

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                search(query)
            }
        }

        if (this.eventIndexList.size == 0) {
            tv_failed_search.visibility = TextView.VISIBLE
            recyclerView_search.visibility = RecyclerView.GONE
        } else {
            tv_failed_search.visibility = TextView.GONE
            recyclerView_search.visibility = RecyclerView.VISIBLE
        }

        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.setHomeButtonEnabled(true)

        viewManager = LinearLayoutManager(this)
        viewAdapter = EventAdapterSearching(this, this.eventIndexList)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView_search).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        recyclerView.addItemDecoration(RecycleViewItemDivider(this))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return item?.let { super.onOptionsItemSelected(it) } == true
    }

    private fun search(query: String) {
        supportActionBar?.title = this.resources.getString(R.string.searching_toolbar_title, query)
        val searchTerms = SearchHandler.splitStringToList(query)
        searchTerms?.forEach {
            this.eventIndexList.addAll(SearchHandler.searchOnEventData(it))
        }
        this.eventIndexList = this.eventIndexList.distinct().toMutableList()
    }
}
