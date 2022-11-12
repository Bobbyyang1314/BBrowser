package com.example.myapplication.Browser.Fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.example.myapplication.Browser.Activity.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentBrowserBinding
import java.io.ByteArrayOutputStream

class BrowserFragment(private var urlNew: String) : Fragment() {

    lateinit var binding: FragmentBrowserBinding
    var webIcon: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_browser, container, false)
        binding = FragmentBrowserBinding.bind(view)

        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()

        val mainRef = requireActivity() as MainActivity

        // Refresh function
        mainRef.binding.refreshButton.visibility = View.VISIBLE
        mainRef.binding.refreshButton.setOnClickListener {
            binding.webView.reload()
        }

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webViewClient = object: WebViewClient(){
                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    mainRef.binding.topSearchBar.text = SpannableStringBuilder(url)
                }


                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    mainRef.binding.progressBar.progress = 0
                    mainRef.binding.progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainRef.binding.progressBar.visibility = View.GONE
                }

            }

            webChromeClient = object:WebChromeClient(){

                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    super.onReceivedIcon(view, icon)
                    try{
                        mainRef.binding.webIcon.setImageBitmap(icon)
                        webIcon = icon

                        // Bookmarks icons
                        MainActivity.bookMarkIndex = mainRef.isBookMarked(view?.url!!)
                        if (MainActivity.bookMarkIndex != -1) {
                            val array = ByteArrayOutputStream()
                            icon!!.compress(Bitmap.CompressFormat.PNG, 100, array)
                            MainActivity.bookMarkList[MainActivity.bookMarkIndex].image = array.toByteArray()
                        }
                    } catch (e : Exception){

                    }
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    binding.webView.visibility = View.GONE
                    binding.customView.visibility = View.VISIBLE
                    binding.customView.addView(view)
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    binding.webView.visibility = View.VISIBLE
                    binding.customView.visibility = View.GONE
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mainRef.binding.progressBar.progress = newProgress
                }
            }

            when{
                URLUtil.isValidUrl(url) -> loadUrl(urlNew)
                urlNew.contains(".com", ignoreCase = true) -> loadUrl(urlNew)
                else -> loadUrl("https://www.google.com/search?q=$urlNew")
            }

        }
    }

    override fun onPause() {
        super.onPause()

        (requireActivity() as MainActivity).saveBookMarks()
        // clear data
        binding.webView.apply {
            clearMatches()
            clearHistory()
            clearFormData()
            clearSslPreferences()
            clearCache(true)

            CookieManager.getInstance().removeAllCookies(null)
            WebStorage.getInstance().deleteAllData()
        }
    }
}