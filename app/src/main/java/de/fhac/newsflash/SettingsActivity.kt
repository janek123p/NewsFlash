package de.fhac.newsflash

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
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

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rssFeedListAdapter = RSSFeedsAdapter(this@SettingsActivity)

        binding.apply {
            btBack.setOnClickListener { onBackPressed() }
            listRssFeeds.adapter = rssFeedListAdapter
            txtRssLink.doAfterTextChanged { onRSSURLChanged() }
        }

        initRSSAutoCompletion()
        addOnAddFeedClickedListener()
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

    private fun initRSSAutoCompletion() {
        GlobalScope.launch {
            val adapter = StringAdapterWithFilter(
                this@SettingsActivity,
                R.layout.dropdown_item,
                readRSSList()
            )
            runOnUiThread { binding.txtRssLink.setAdapter(adapter) }
        }
    }

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

    private fun addOnAddFeedClickedListener() {
        binding.btAddRssFeed.setOnClickListener {
            binding.loadingIndicator.visibility = View.VISIBLE

            GlobalScope.launch {
                try {
                    SourceController.registerSource(binding.txtRssLink.text.toString())
                    runOnUiThread {
                        clearRSSLinkError()
                        binding.txtRssLink.setText("")
                        binding.loadingIndicator.visibility = View.GONE
                        currentFocus?.let { view ->
                            val imm =
                                getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.hideSoftInputFromWindow(view.windowToken, 0)
                        }
                    }
                } catch (ex: Exception) {
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
}