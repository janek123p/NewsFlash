package de.fhac.newsflash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var newsList: List<News>
    private var newsListAdapter: NewsListAdapter? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBinding: BottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private var currentNews: News? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bottomSheetBinding = binding.bottomSheet
        setContentView(binding.root)

        loadNewsData()
        initBottomSheet()
        addCallbacks()
        addBottomNavigationCallback()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("activatedNews")) {
            val news = savedInstanceState.getParcelable<News>("activatedNews")
            news?.apply { showDetailedNews(this) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        currentNews?.apply {
            outState.putParcelable("activatedNews", this)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestart() {
        refreshNewsData()
        super.onRestart()
    }

    private fun addBottomNavigationCallback() {
        binding.bottomNavigation.apply {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.settings -> {
                        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(intent)
                        return@setOnItemSelectedListener true
                    }
                }
                return@setOnItemSelectedListener false
            }
        }
    }

    private fun loadNewsData() {
        binding.loadingIndicatorTop.visibility = View.VISIBLE
        GlobalScope.launch {
            newsList = NewsController.getNews(refresh = false)
            runOnUiThread {
                newsListAdapter = NewsListAdapter(this@MainActivity, newsList)
                binding.newsList.adapter = newsListAdapter

                newsListAdapter!!.notifyDataSetChanged()
                binding.loadingIndicatorTop.visibility = View.GONE
            }
        }
    }

    private fun refreshNewsData() {
        binding.loadingIndicatorTop.visibility = View.VISIBLE
        GlobalScope.launch {
            newsList = NewsController.getNews(refresh = true)
            runOnUiThread {
                newsListAdapter?.notifyDataSetChanged()
                binding.loadingIndicatorTop.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetBinding.bottomSheetRootLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBinding.webContent.webViewClient = WebViewClient()
        bottomSheetBinding.webContent.settings.apply {
            domStorageEnabled = true
            loadsImagesAutomatically = true
            javaScriptEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
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

        bottomSheetBinding.btSave.setOnClickListener { saveOrRemoveCurrentNewsToFavorites() }
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
            if (news in NewsController.getFavorites()) {
                btSave.setBackgroundResource(R.drawable.ic_baseline_star_24)
            } else {
                btSave.setBackgroundResource(R.drawable.ic_baseline_star_border_24)
            }
            webScrollView.scrollY = 0
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
            val shareMessage = getString(R.string.read_this_article)+"\n\n${news.url}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.which_app_to_share)))
        }
    }

    private fun showCurrentNewsInBrowser() {
        currentNews?.let { news ->
            val showInBrowserIntent = Intent(Intent.ACTION_VIEW)
            showInBrowserIntent.data = Uri.parse(news.url)
            startActivity(showInBrowserIntent)
        }
    }

    private fun saveOrRemoveCurrentNewsToFavorites() {
        currentNews?.let { news ->
            if (news in NewsController.getFavorites()) {
                NewsController.removeFavorite(news.id)
                Toast.makeText(this@MainActivity, R.string.removed_from_favs, Toast.LENGTH_SHORT)
                    .show()
                bottomSheetBinding.btSave.setBackgroundResource(R.drawable.ic_baseline_star_border_24)
            } else {
                NewsController.addFavorite(news.id)
                Toast.makeText(this@MainActivity, R.string.saved_to_favs, Toast.LENGTH_SHORT).show()
                bottomSheetBinding.btSave.setBackgroundResource(R.drawable.ic_baseline_star_24)
            }
        }
    }
}