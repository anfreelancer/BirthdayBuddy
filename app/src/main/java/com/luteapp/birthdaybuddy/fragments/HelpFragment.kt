package com.luteapp.birthdaybuddy.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.luteapp.birthdaybuddy.MainActivity
import com.luteapp.birthdaybuddy.R
import com.luteapp.birthdaybuddy.views.HelpAdapter

class HelpFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val toolbar: Toolbar by lazy {
        activity!!.findViewById<Toolbar>(R.id.toolbar)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (context as MainActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        (context as MainActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        (context as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewManager =
            LinearLayoutManager(view.context)
        viewAdapter = HelpAdapter(context!!)

        toolbar.setTitle(R.string.main_menu_item_help)

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_help).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                (context as MainActivity).supportFragmentManager.popBackStackImmediate()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HelpFragment()
    }
}
