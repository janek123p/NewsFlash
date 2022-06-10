package de.fhac.newsflash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.webkit.URLUtil
import de.fhac.newsflash.data.controller.SourceController
import de.fhac.newsflash.databinding.ActivitySettingsBinding
import kotlinx.coroutines.runBlocking

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
        }

        addOnAddFeedClickedListener()
    }

    private fun setRSSLinkError(message : String){
        binding.wrapperTxtLink.isErrorEnabled = true
        binding.wrapperTxtLink.error = message
    }

    private fun clearRSSLinkError(){
        binding.wrapperTxtLink.isErrorEnabled = false
    }

    private fun addOnAddFeedClickedListener() {
        binding.btAddRssFeed.setOnClickListener{
            runBlocking {
                try{
                    SourceController.registerSource(binding.txtRssLink.text.toString());
                    rssFeedListAdapter.updateFeeds();
                    clearRSSLinkError()
                    binding.txtRssLink.setText("")
                }catch(ex: Exception){
                    setRSSLinkError(ex.message ?: "Unbekannter Fehler");
                }
            }
        }
    }
}