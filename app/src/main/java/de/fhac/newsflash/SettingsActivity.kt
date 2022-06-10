package de.fhac.newsflash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.webkit.URLUtil
import de.fhac.newsflash.data.controller.SourceController
import de.fhac.newsflash.data.models.RSSSource
import de.fhac.newsflash.databinding.ActivityMainBinding
import de.fhac.newsflash.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var rssFeedListAdapter: RSSFeedsAdapterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rssFeedListAdapter = RSSFeedsAdapterAdapter(this@SettingsActivity)

        binding.apply {
            btBack.setOnClickListener { onBackPressed() }
            listRssFeeds.adapter = rssFeedListAdapter
        }

        addOnAddFeedClickedListener()
        addCheckRSSFeedInputListener()
    }

    private fun addCheckRSSFeedInputListener() {
        binding.txtRssLink.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text : Editable?) {
                var link = text.toString()
                if(!URLUtil.isValidUrl(link)){
                    setRSSLinkError("Ungültige URL!")
                    return
                }
                setRSSLinkValid()
            }

        })
    }

    private fun setRSSLinkError(message : String){
        binding.wrapperTxtLink.isErrorEnabled = true
        binding.wrapperTxtLink.error = message
    }

    private fun setRSSLinkValid(){
        binding.wrapperTxtLink.isErrorEnabled = false
        binding.wrapperTxtLink.helperText = "Gültiger RSS-Feed!"
    }

    private fun addOnAddFeedClickedListener() {
        binding.btAddRssFeed.setOnClickListener{
            TODO("Noch nicht implementiert")
        }
    }
}