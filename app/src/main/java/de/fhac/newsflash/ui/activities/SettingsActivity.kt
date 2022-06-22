package de.fhac.newsflash.ui.activities

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import de.fhac.newsflash.R
import de.fhac.newsflash.ui.adapter.RSSFeedsAdapter
import de.fhac.newsflash.ui.adapter.StringAdapterWithFilter
import de.fhac.newsflash.data.controller.SourceController
import de.fhac.newsflash.data.models.RSSSource
import de.fhac.newsflash.databinding.ActivitySettingsBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.streams.toList

/**
 * Activity to handle app settings, especially managing user selected RSS feeds
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var rssFeedListAdapter: RSSFeedsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate binding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create RSSFeedsAdapter
        rssFeedListAdapter = RSSFeedsAdapter(this@SettingsActivity)

        // Add callbacks and set adapter fo ListView
        binding.apply {
            btBack.setOnClickListener { onBackPressed() }
            listRssFeeds.adapter = rssFeedListAdapter
            txtRssLink.doAfterTextChanged { onRSSURLChanged() }
            binding.btAddRssFeed.setOnClickListener { onAddFeedClickedListener() }
        }

        // Initialize autocompletion
        initRSSAutoCompletion()
    }

    /**
     * Resume subscription if Activity is restarted
     */
    override fun onRestart() {
        rssFeedListAdapter.resumeSubscription()
        super.onRestart()
    }

    /**
     * Pause subscription if Activity is stopped
     */
    override fun onStop() {
        rssFeedListAdapter.pauseSubscription()
        super.onStop()
    }

    /**
     * Read list of predefined RSS feeds
     */
    private suspend fun readRSSList(): MutableList<String> {
        val result = GlobalScope.async {
            resources.openRawResource(R.raw.rss_feeds).bufferedReader().use { reader ->
                return@async reader.lines()
                    .filter { line -> !line.startsWith("//", ignoreCase = true) }.toList()
                    .toMutableList()
            }
        }
        return result.await()
    }

    /**
     * Initialize RSS autocompletion by setting corresponding adapter
     */
    private fun initRSSAutoCompletion() {
        GlobalScope.launch {
            val adapter = StringAdapterWithFilter(
                this@SettingsActivity,
                readRSSList()
            )
            runOnUiThread { binding.txtRssLink.setAdapter(adapter) }
        }
    }

    /**
     * Function to determine action when types RSS URL changed
     */
    private fun onRSSURLChanged() {
        val url = binding.txtRssLink.text.toString()
        GlobalScope.launch {
            if (url.isEmpty() || RSSSource.isValidRSSLink(url)) {
                runOnUiThread { clearRSSLinkError() }
            } else {
                runOnUiThread { setRSSLinkError(getString(R.string.invalid_rss_feed)) }
            }
        }
    }

    private fun setRSSLinkError(message: String) {
        binding.wrapperTxtLink.isErrorEnabled = true
        binding.wrapperTxtLink.error = message
    }

    private fun clearRSSLinkError() {
        binding.wrapperTxtLink.isErrorEnabled = false
    }

    /**
     * Function that determines behavior when addFeed button is clicked
     */
    private fun onAddFeedClickedListener() {
        binding.loadingIndicator.visibility = View.VISIBLE

        GlobalScope.launch {
            try {
                SourceController.registerSource(binding.txtRssLink.text.toString())
                runOnUiThread {
                    // Clear error, remove loading indicator and clear url EditText
                    clearRSSLinkError()
                    binding.txtRssLink.setText("")
                    binding.loadingIndicator.visibility = View.GONE
                    // Eventually hide soft keyboard
                    currentFocus?.let { view ->
                        val imm =
                            getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            } catch (ex: Exception) {
                // If registration of RSS source has failed
                runOnUiThread {
                    setRSSLinkError(
                        ex.message ?: getString(R.string.error_while_adding_rss_feed)
                    )
                    binding.loadingIndicator.visibility = View.GONE
                }
            }
        }


    }
}