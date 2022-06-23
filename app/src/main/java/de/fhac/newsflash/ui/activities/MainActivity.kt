package de.fhac.newsflash.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.fhac.newsflash.R
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.repositories.AppDatabase
import de.fhac.newsflash.databinding.ActivityMainBinding
import de.fhac.newsflash.databinding.BottomSheetBinding
import de.fhac.newsflash.ui.NewsBottomSheetCallback
import de.fhac.newsflash.ui.UIExtensions.Companion.setOnClickListenerWithAnimation
import de.fhac.newsflash.ui.adapter.FilterAdapter
import de.fhac.newsflash.ui.adapter.NewsListAdapter
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

class MainActivity : AppCompatActivity() {
    private var filterFavourites: Boolean = false
    private lateinit var newsListAdapter: NewsListAdapter
    lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBinding: BottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private var currentNews: News? = null
    private lateinit var filterAdapter: FilterAdapter
    private var currentFilter: Filter? = null


    /**
     * Initialize UI and logic of MainActivity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bottomSheetBinding = binding.bottomSheet
        setContentView(binding.root)

        val pullToRefresh: SwipeRefreshLayout = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener {
            reloadNewsData()
        }
        AppDatabase.initDatabase(applicationContext)
        initFilters()
        initNewsData()
        initBottomSheet()
        addCallbacks()
        addNavigationCallbacks()
    }

    /**
     * Restore lastly loaded news e.g. when device is rotated
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("activatedNews")) {
            val news = savedInstanceState.getParcelable<News>("activatedNews")
            news?.apply { showDetailedNews(this) }
        }
    }

    /**
     * Save currently loaded news to be able to restore it in case the device is rotated or similar
     */
    override fun onSaveInstanceState(outState: Bundle) {
        currentNews?.apply {
            outState.putParcelable("activatedNews", this)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Reload data if app is restarted and resume news subscriptions
     */
    override fun onRestart() {
        reloadNewsData()
        newsListAdapter.resumeSubscriptions()
        super.onRestart()
    }

    /**
     * Cancel subscriptions if app is stopped
     */
    override fun onStop() {
        newsListAdapter.pauseSubscriptions()
        super.onStop()
    }


    /**
     * Add Callbacks for bottom navigation bar
     */
    private fun addNavigationCallbacks() {
        binding.bottomNavigation.apply {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.favorites_nav -> {
                        filterFavourites = true
                        reloadNewsData()
                        return@setOnItemSelectedListener true
                    }
                }

                when (it.itemId) {
                    R.id.news_nav -> {
                        filterFavourites = false
                        reloadNewsData()
                        return@setOnItemSelectedListener true
                    }
                }
                return@setOnItemSelectedListener false
            }
        }

        binding.titleBar.settingsNav.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Initialize ListView and Adapter and load news into it
     */
    private fun initNewsData() {
        newsListAdapter = NewsListAdapter(this)
        binding.newsList.adapter = newsListAdapter
        reloadNewsData()
    }

    /**
     * Reload News
     */
    private fun reloadNewsData() {
        newsListAdapter.launchReloadData()
    }

    /**
     * Initializes and loads Filters
     */
    private fun initFilters() {
        filterAdapter = FilterAdapter(this)
        binding.filter.sourceFilterContainer.adapter = filterAdapter
    }

    /**
     * Initialize Bottom Sheet
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetBinding.bottomSheetRootLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBinding.apply {
            webContent.webViewClient = WebViewClient()
            webContent.webChromeClient = ChromeClient(progressBar, webCardView)
            webContent.settings.apply {
                domStorageEnabled = true
                loadsImagesAutomatically = true
                javaScriptEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
        }
    }

    /**
     * Add various callbacks for bottom sheet, BackPressedDispatcher and various buttons
     */
    private fun addCallbacks() {
        // Add bottom sheet callback
        bottomSheetBehavior.addBottomSheetCallback(
            NewsBottomSheetCallback(
                bottomSheetBinding,
                this
            )
        )

        // Add callback to handle back presses
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@MainActivity.handleOnBackPressed()
            }
        })

        // Add callbacks for bottom sheet buttons
        bottomSheetBinding.apply {
            btResizeNews.setOnClickListener { switchBottomSheetBehaviorState() }
            btShare.setOnClickListenerWithAnimation { shareCurrentNews() }
            btShowInBrowser.setOnClickListenerWithAnimation { showCurrentNewsInBrowser() }
            btSave.setOnClickListenerWithAnimation { saveOrRemoveCurrentNewsToFavorites() }
            btRefresh.setOnClickListenerWithAnimation { refreshCurrentNews() }
        }

        // Add filter callback
        binding.filter.apply {
            filterDropdownButton.setOnClickListenerWithAnimation {
                if (filters.visibility == View.VISIBLE) {
                    filters.visibility = View.GONE
                } else if (filters.visibility == View.GONE) {
                    filters.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Reloads URL in WebView
     */
    private fun refreshCurrentNews() {
        bottomSheetBinding.webContent.loadUrl(currentNews!!.url)
    }

    /**
     * Sets background (news list, filters etc.) blurred
     */
    fun setBackgroundBlurred(value: Float) {
        var valueInRange = if (value < 0f) 0f else value
        valueInRange = if (value > 1f) 1f else valueInRange

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.mainRelativLayout.setRenderEffect(
                if (value < .1) null else RenderEffect.createBlurEffect(
                    valueInRange * 10f,
                    valueInRange * 10f,
                    Shader.TileMode.MIRROR
                )
            )
        }
    }

    /**
     * Shows news in detail view inside bottom sheet
     */
    fun showDetailedNews(news: News) {
        currentNews = news

        bottomSheetBinding.apply {
            txtHeading.text = Jsoup.clean(news.title, Safelist.none()) //Remove possible HTML tags

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //Show description with possible html tags, removes everything but text format
                txtShortMessage.text = Html.fromHtml(
                    Jsoup.clean(news.description, Safelist.basic()),
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                txtShortMessage.text =
                    Html.fromHtml(Jsoup.clean(news.description, Safelist.basic()))
            }

            progressBar.visibility = View.VISIBLE
            webCardView.visibility = View.GONE

            webContent.loadUrl(news.url)

            if (news.imageUrl != null) {
                cardViewThumbnail.visibility = View.VISIBLE
                Glide.with(this@MainActivity).load(news.imageUrl).centerCrop().into(imgThumbnail)
            } else {
                cardViewThumbnail.visibility = View.GONE
            }

            if (NewsController.getFavoritesStream().getLatest()?.contains(news) == true) {
                btSave.setBackgroundResource(R.drawable.ic_baseline_star_24)
            } else {
                btSave.setBackgroundResource(R.drawable.ic_baseline_star_border_24)
            }
            webScrollView.scrollY = 0
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }


    /**
     * Method gets called, when back-key is pressed on device
     */
    private fun handleOnBackPressed() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_HIDDEN
            BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_COLLAPSED
            else -> this@MainActivity.onBackPressed()
        }
    }

    /**
     * Switches state of the bottomSheetBehavior from COLLAPSED to EXPANDED and vice versa
     */
    private fun switchBottomSheetBehaviorState() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    /**
     * Opens menu for user to share news with various applications
     */
    private fun shareCurrentNews() {
        currentNews?.let { news ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "NewsFlash")
            val shareMessage = getString(R.string.read_this_article) + "\n\n${news.url}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.which_app_to_share)))
        }
    }

    /**
     * Opens current news in browser
     */
    private fun showCurrentNewsInBrowser() {
        if (currentNews != null && currentNews!!.url.isNotEmpty()) {
            try {
                CustomTabsIntent.Builder()
                    .setInstantAppsEnabled(false)
                    .setUrlBarHidingEnabled(true)
                    .setShowTitle(false)
                    .build()
                    .launchUrl(this, Uri.parse(currentNews!!.url))
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW)
                browserIntent.data = Uri.parse(currentNews!!.url)
                startActivity(browserIntent)
            }
        }
    }

    /**
     * Depending on if the current news already belongs to favorites, the news will be saved to or removed from favorites
     */
    private fun saveOrRemoveCurrentNewsToFavorites() {
        currentNews?.let { news ->
            if (NewsController.getFavoritesStream().getLatest()?.contains(news) == true) {
                NewsController.removeFavorite(news)
                Toast.makeText(this@MainActivity, R.string.removed_from_favs, Toast.LENGTH_SHORT)
                    .show()
                bottomSheetBinding.btSave.setBackgroundResource(R.drawable.ic_baseline_star_border_24)
            } else {
                NewsController.addFavorite(news)
                Toast.makeText(this@MainActivity, R.string.saved_to_favs, Toast.LENGTH_SHORT).show()
                bottomSheetBinding.btSave.setBackgroundResource(R.drawable.ic_baseline_star_24)
            }
        }
    }

    /**
     * Resets the current news to be null
     */
    fun resetCurrentNews() {
        currentNews = null
    }

    /**
     * Class to implement WebChromeClient with specific onProgressChanged
     */
    private class ChromeClient(
        private val progressBar: ProgressBar,
        private val webCardView: CardView
    ) :
        WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if (newProgress >= 90) {
                progressBar.visibility = View.GONE
                webCardView.visibility = View.VISIBLE
            }

            super.onProgressChanged(view, newProgress)
        }
    }
}