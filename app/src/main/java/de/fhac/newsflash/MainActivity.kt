package de.fhac.newsflash

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.databinding.ActivityMainBinding
import de.fhac.newsflash.databinding.BottomSheetBinding
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {
    private lateinit var newsList: List<News>
    private lateinit var newsListAdapter: NewsListAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBinding: BottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private var currentNews: News? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bottomSheetBinding = binding.bottomSheet
        setContentView(binding.root)

        initNewsData()
        initBottomSheet()
        addCallbacks()
        addBottomNavigationCallback()
    }

    private fun addBottomNavigationCallback() {
        binding.bottomNavigation.apply {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.settings -> {
                        var intent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(intent)
                        return@setOnItemSelectedListener true
                    }
                }
                return@setOnItemSelectedListener false
            }
        }
    }

    private fun initNewsData() {
        runBlocking {
            newsList = NewsController.getNews()
            newsListAdapter = NewsListAdapter(this@MainActivity, newsList)
            binding.newsList.adapter = newsListAdapter
            newsListAdapter.notifyDataSetChanged()
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetBinding.bottomSheetRootLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBinding.webContent.webViewClient = WebViewClient()
    }

    private fun addCallbacks() {
        bottomSheetBehavior.addBottomSheetCallback(
            NewsBottomSheetCallback(
                bottomSheetBinding,
                this
            )
        )

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@MainActivity.handleOnBackPressed()
            }
        })

        bottomSheetBinding.btResizeNews.setOnClickListener { resizeNews() }

        bottomSheetBinding.btShare.setOnClickListener { shareCurrentNews() }

        bottomSheetBinding.btShowInBrowser.setOnClickListener { showCurrentNewsInBrowser() }

        bottomSheetBinding.btSave.setOnClickListener { saveCurrentNewsToFavourite() }
    }

    fun setBackgroundBlurred(value: Float) {
        var valueInRange = if (value < 0f) 0f else value
        valueInRange = if (value > 1f) 1f else valueInRange

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.mainConstraintLayout.setRenderEffect(
                if (value < .1) null else RenderEffect.createBlurEffect(
                    valueInRange * 10f,
                    valueInRange * 10f,
                    Shader.TileMode.MIRROR
                )
            )
        }
    }

    fun showDetailedNews(news: News) {
        currentNews = news

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


    private fun handleOnBackPressed() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_HIDDEN
            BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_COLLAPSED
            else -> this@MainActivity.onBackPressed()
        }
    }

    private fun resizeNews() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun shareCurrentNews() {
        currentNews?.let { news ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "NewsFlash")
            var shareMessage = "\nLies dir diesen Artikel durch:\n\n${news.url}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Welche App soll verwendet werden?"))
        }
    }

    private fun showCurrentNewsInBrowser() {
        currentNews?.let { news ->
            val showInBrowserIntent = Intent(Intent.ACTION_VIEW)
            showInBrowserIntent.data = Uri.parse(news.url)
            startActivity(showInBrowserIntent)
        }
    }

    private fun saveCurrentNewsToFavourite() {
        currentNews?.let { news ->
            NewsController.addFavorite(news.id)
            Toast.makeText(this@MainActivity, R.string.saved_to_favs, Toast.LENGTH_LONG).show()
        }
    }
}