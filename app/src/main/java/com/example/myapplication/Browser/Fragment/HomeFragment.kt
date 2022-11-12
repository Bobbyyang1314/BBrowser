package com.example.myapplication.Browser.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.Browser.Activity.BookMarkActivity
import com.example.myapplication.Browser.Activity.MainActivity
import com.example.myapplication.Browser.Activity.changeTab
import com.example.myapplication.Browser.Activity.checkForInternet
import com.example.myapplication.Browser.Adaptor.BookmarkAdapter
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var binding:FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)

        return view
    }

    override fun onResume() {
        super.onResume()

        val mainActivityRef = requireActivity() as MainActivity
        mainActivityRef.binding.topSearchBar.setText("")
        binding.searchView.setQuery("", false)
        mainActivityRef.binding.webIcon.setImageResource(R.drawable.ic_baseline_search_24)

        mainActivityRef.binding.refreshButton.visibility = View.GONE

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(result : String?): Boolean {
                if (checkForInternet(requireContext()))
                    changeTab(result!!, BrowserFragment(result))
                else
                    Snackbar.make(binding.root, "No Internet", 3000).show()
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean = false
        })


        mainActivityRef.binding.goButton.setOnClickListener{
            if (checkForInternet(requireContext()))
                changeTab(mainActivityRef.binding.topSearchBar.text.toString(),
                    BrowserFragment(mainActivityRef.binding.topSearchBar.text.toString())
                )
            else
                Snackbar.make(binding.root, "No Internet", 3000).show()
        }






        binding.recycleView.setHasFixedSize(true)
        binding.recycleView.setItemViewCacheSize(5)
        binding.recycleView.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.recycleView.adapter = BookmarkAdapter(requireContext())


        if(MainActivity.bookMarkList.size < 1)
            binding.viewAllBtn.visibility = View.GONE
        binding.viewAllBtn.setOnClickListener {
            startActivity(Intent(requireContext(), BookMarkActivity::class.java))
        }


    }
}