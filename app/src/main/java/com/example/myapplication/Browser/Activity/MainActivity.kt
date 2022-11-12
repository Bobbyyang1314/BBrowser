package com.example.myapplication.Browser.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

import com.example.myapplication.Browser.Activity.MainActivity.Companion.paper

import com.example.myapplication.Browser.Fragment.BrowserFragment
import com.example.myapplication.Browser.Fragment.HomeFragment
import com.example.myapplication.Browser.Model.Bookmarks
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.BookmarkDialogBinding
import com.example.myapplication.databinding.FeaturesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {


    lateinit var binding : ActivityMainBinding

    companion object{
        var tabsList: ArrayList<Fragment> = ArrayList()
        var bookMarkList: ArrayList<Bookmarks> = ArrayList()
        var bookMarkIndex: Int = -1
        lateinit var paper : ViewPager2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        getAllBookMarks()

//        how many pages
        tabsList.add(HomeFragment())
        binding.paper.adapter = TabsAdapter(supportFragmentManager, lifecycle)
        binding.paper.isUserInputEnabled = false
        paper = binding.paper
        initializeView()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {

        var fraq: BrowserFragment? = null
        try {
            fraq = tabsList[binding.paper.currentItem] as BrowserFragment
        }catch (e:Exception){}


        when{
            fraq?.binding?.webView?.canGoBack() == true -> fraq.binding.webView.goBack()
            binding.paper.currentItem != 0 -> {
                tabsList.removeAt(binding.paper.currentItem)
                binding.paper.adapter?.notifyDataSetChanged()
                binding.paper.currentItem = tabsList.size - 1

            }
            else -> super.onBackPressed()
        }
    }


    private inner class TabsAdapter(fa: FragmentManager, lc: Lifecycle) : FragmentStateAdapter(fa, lc) {
        override fun getItemCount(): Int = tabsList.size

        override fun createFragment(position: Int): Fragment = tabsList[position]
    }



    private fun initializeView() {
        binding.settingButton.setOnClickListener {
            var fraq: BrowserFragment? = null
            try {
                fraq = tabsList[binding.paper.currentItem] as BrowserFragment
            }catch (e:Exception){}

            val view = layoutInflater.inflate(R.layout.features, binding.root, false)
            val dialogBinding = FeaturesBinding.bind(view)

            val dialog = MaterialAlertDialogBuilder(this).setView(view).create()

            dialog.window?.apply {
                attributes.gravity = Gravity.BOTTOM
                attributes.y = 50
                setBackgroundDrawable(ColorDrawable(0xFFFFFF.toInt()))
            }

            dialog.show()




            fraq?.let {
                bookMarkIndex = isBookMarked(it.binding.webView.url!!)
                if (bookMarkIndex != -1) {
                    dialogBinding.bookmarkBtn.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.cool_blue))
                }
            } }


            dialogBinding.backBtn.setOnClickListener {
                onBackPressed()
            }

            dialogBinding.forwardBtn.setOnClickListener{
                fraq?.apply {
                    if (binding.webView.canGoForward())
                        binding.webView.goForward()
                }
            }

            dialogBinding.bookmarkBtn.setOnClickListener{
                fraq?.let{

                    if (bookMarkIndex == -1) {
                        val viewB = layoutInflater.inflate(R.layout.bookmark_dialog, binding.root, false)

                        val bBinding = BookmarkDialogBinding.bind(viewB)

                        val dialogB = MaterialAlertDialogBuilder(this)
                            .setTitle("Add Bookmark")
                            .setMessage("URL:${it.binding.webView.url}")
                            .setPositiveButton("Add"){self, _ ->
                                try {
                                    val array = ByteArrayOutputStream()
                                    it.webIcon?.compress(Bitmap.CompressFormat.PNG, 100, array)
                                    bookMarkList.add(
                                        Bookmarks(name = bBinding.bookMarkTitle.text.toString(), url = it.binding.webView.url!!, array.toByteArray()))
                                } catch(e : Exception) {
                                    bookMarkList.add(
                                        Bookmarks(name = bBinding.bookMarkTitle.text.toString(), url = it.binding.webView.url!!))
                                }
                                self.dismiss()}
                            .setNegativeButton("Cancel"){self, _ -> self.dismiss()}
                            .setView(viewB).create()
                        dialogB.show()
                        bBinding.bookMarkTitle.setText(it.binding.webView.title)
                    } else {
                        val dialogB = MaterialAlertDialogBuilder(this)
                            .setTitle("Delete Bookmark")
                            .setMessage("URL:${it.binding.webView.url}")
                            .setPositiveButton("Delete"){self, _ ->
                                bookMarkList.removeAt(bookMarkIndex)
                                self.dismiss()}
                            .setNegativeButton("Cancel"){self, _ -> self.dismiss()}
                            .create()
                        dialogB.show()
                    }
                }
                dialog.dismiss()
            }
        }
    }


    fun isBookMarked(url: String): Int {
        bookMarkList.forEachIndexed { index, bookmark ->
            if (bookmark.url == url) return index
        }
        return -1
    }

    fun saveBookMarks() {
        val editor = getSharedPreferences("BOOKMARKS", MODE_PRIVATE).edit()

        val data = GsonBuilder().create().toJson(bookMarkList)
        editor.putString("bookMarkList", data)

        editor.apply()
    }

    fun getAllBookMarks() {

        bookMarkList = ArrayList()

        val editor = getSharedPreferences("BOOKMARKS", MODE_PRIVATE)
        val data = editor.getString("bookMarkList", null)
        if (data != null) {
            val list : ArrayList<Bookmarks> = GsonBuilder().create().fromJson(data, object: TypeToken<ArrayList<Bookmarks>>(){}.type)
            bookMarkList.addAll(list)
        }
    }
}

@SuppressLint("NotifyDataSetChanged")
fun changeTab(url: String, fragment: Fragment) {
    MainActivity.tabsList.add(fragment)
    paper.adapter?.notifyDataSetChanged()
    paper.currentItem = MainActivity.tabsList.size - 1
}

fun checkForInternet(context : Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when{
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}