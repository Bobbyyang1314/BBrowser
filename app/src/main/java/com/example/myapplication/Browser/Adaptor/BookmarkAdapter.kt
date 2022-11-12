package com.example.myapplication.Browser.Adaptor

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Browser.Activity.MainActivity
import com.example.myapplication.Browser.Activity.changeTab
import com.example.myapplication.Browser.Activity.checkForInternet
import com.example.myapplication.Browser.Fragment.BrowserFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.BookmarkBinding
import com.example.myapplication.databinding.LongBookmarkBinding
import com.google.android.material.snackbar.Snackbar

class BookmarkAdapter(private val context : Context, private val isActivity: Boolean = false):
    RecyclerView.Adapter<BookmarkAdapter.MyHolder>() {

    private val colors = context.resources.getIntArray(R.array.myColors)

    class MyHolder(binding: BookmarkBinding? = null, bindingL: LongBookmarkBinding? = null):RecyclerView.ViewHolder((binding?.root ?: bindingL?.root)!!) {
        val image = (binding?.bookmarkIcon ?: bindingL?.bookmarkIcon)!!
        val name = (binding?.bookmarkName ?: bindingL?.bookmarkName)!!
        val root = (binding?.root ?: bindingL?.root)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        if(isActivity)
            return MyHolder(bindingL = LongBookmarkBinding.inflate(LayoutInflater.from(context), parent, false))
        return MyHolder(binding = BookmarkBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: BookmarkAdapter.MyHolder, position: Int) {
        // bookmarks colors
        try {
            val icon = BitmapFactory.decodeByteArray(MainActivity.bookMarkList[position].image, 0,
                MainActivity.bookMarkList[position].image!!.size)
            holder.image.background = icon.toDrawable(context.resources)
        } catch (e:Exception) {
            holder.image.setBackgroundColor(colors[(colors.indices).random()])
            holder.image.text = MainActivity.bookMarkList[position].name[0].toString()
        }
        holder.name.text = MainActivity.bookMarkList[position].name

        holder.root.setOnClickListener{

            when{
                checkForInternet(context) -> {
                    changeTab(MainActivity.bookMarkList[position].name,
                        BrowserFragment(urlNew = MainActivity.bookMarkList[position].url))
                    if (isActivity) (context as Activity).finish()
                }
                else -> Snackbar.make(holder.root, "No Internet", 3000).show()
            }

        }
    }

    override fun getItemCount(): Int {
        return MainActivity.bookMarkList.size
    }

}