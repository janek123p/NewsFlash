package de.fhac.newsflash

import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.databinding.ActivityMainBinding
import de.fhac.newsflash.databinding.BottomSheetBinding


class MainActivity : AppCompatActivity() {
    private lateinit var newsList: List<News>
    private lateinit var newsListAdapter: NewsListAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBinding: BottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bottomSheetBinding = binding.bottomSheet
        setContentView(binding.root)

        initNewsData()
        initBottomSheetBehavior()
        addCallbacks()
    }

    private fun initNewsData(){
        newsList = NewsController.getNews()
        newsListAdapter = NewsListAdapter(this, newsList)
        binding.newsList.adapter = newsListAdapter
        newsListAdapter.notifyDataSetChanged()
    }
    private fun initBottomSheetBehavior(){
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetBinding.bottomSheetRootLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBinding.webContent.webViewClient = WebViewClient()
    }
    private fun addCallbacks() {
        bottomSheetBehavior.addBottomSheetCallback(NewsBottomSheetCallback(bottomSheetBinding))
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                when(bottomSheetBehavior.state){
                    BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    else -> this@MainActivity.onBackPressed()
                }
            }
        })
    }

    fun showDetailedNews(news: News) {
        bottomSheetBinding.apply {
            txtHeading.text = news.name
            txtShortMessage.text = news.description
            webContent.loadUrl(news.url)
            if (news.imageUrl != null) {
                Glide.with(this@MainActivity).load(news.imageUrl).centerCrop().into(imgThumbnail)
            }
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

}