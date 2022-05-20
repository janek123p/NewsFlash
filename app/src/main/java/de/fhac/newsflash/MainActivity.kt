package de.fhac.newsflash

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var newsList: List<News>
    lateinit var newsListAdapter: NewsListAdapter
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val controller = NewsController();
        newsList = controller.getNews();
        newsListAdapter = NewsListAdapter(this, newsList)
        binding.newsList.adapter = newsListAdapter
        newsListAdapter.notifyDataSetChanged()

        BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_HIDDEN;
        binding.webContent.setWebViewClient(WebViewClient())
    }

    fun showDetailedNews(news : News){
        binding.apply {
            txtHeading.text = news.name
            txtShortMessage.text = news.description
            webContent.loadUrl(news.url)
            if (news.imageUrl != null){
                Glide.with(this@MainActivity).load(news.imageUrl).into(binding.imgNews);
            }
        }

        val behavior = BottomSheetBehavior.from(binding.bottomSheet);
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED;
    }

}