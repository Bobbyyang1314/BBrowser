package com.example.myapplication.Browser.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Browser.Adaptor.BookmarkAdapter
import com.example.myapplication.databinding.ActivityBookMarkBinding

class BookMarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBookMarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvBookmarks.setItemViewCacheSize(5)
        binding.rvBookmarks.hasFixedSize()
        binding.rvBookmarks.layoutManager = LinearLayoutManager(this)
        binding.rvBookmarks.adapter = BookmarkAdapter(this, isActivity = true)
    }
}