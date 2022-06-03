package de.fhac.newsflash.data.service

import android.R
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Xml
import androidx.lifecycle.ViewModel
import de.fhac.newsflash.data.models.News
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL
import java.security.KeyStore

object RssService {


    fun parse(url: String, callback: (List<News>) -> Unit) {
        kotlin.run {
            runBlocking {
                launch {
                    val parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(URL(url).openConnection().getInputStream(), "UTF-8");
                    parser.nextTag();

                    callback(readFeed(parser));
                }
            }
        }
    }

    private fun readFeed(parser: XmlPullParser): List<News> {
        val news = mutableListOf<News>();

        parser.require(XmlPullParser.START_TAG, null, "feed")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.name.equals("item", true)) {
                news.add(readEntry(parser))
            }
        }

        return news;
    }

    private fun readEntry(parser: XmlPullParser): News {
        parser.require(XmlPullParser.START_TAG, null, "item")
        var title: String? = null;
        var desc: String? = null;
        var link: String? = null;
        var imageUrl: String? = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue;

            when (parser.name) {
                "title" -> title = readTitle(parser);
                "description" -> desc = readDescription(parser);
                "link" -> link = readNewsLink(parser)
            }

        }

        return News(
            name = title ?: "",
            description = desc ?: "",
            url = link ?: "",
            imageUrl = imageUrl
                ?: "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.stern.de%2Fpanorama%2F50-jahre-doener--das-wichtigste-zur-deutschen-leibspeise-31721190.html&psig=AOvVaw29slUOHj-h87mvHSOazm-e&ust=1654334553253000&source=images&cd=vfe&ved=0CAwQjRxqFwoTCMDhuc_6kPgCFQAAAAAdAAAAABAD"
        )
    }

    // Processes title tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, "title")
        return title
    }

    // Processes news link tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readNewsLink(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "link")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, "link")
        return summary
    }

    // Processes image link tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readImageLink(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "summary")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, "summary")
        return summary
    }

    // Processes summary tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDescription(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "description")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, "description")
        return summary
    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }


}