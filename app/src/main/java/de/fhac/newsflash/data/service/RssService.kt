package de.fhac.newsflash.data.service

import android.R
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Xml
import androidx.lifecycle.ViewModel
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.models.RSSSource
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL
import kotlinx.coroutines.*;
import org.jsoup.Jsoup
import java.security.KeyStore
import java.util.concurrent.Flow

object RssService {

    suspend fun parseMeta(url: String): String? {
        val one = GlobalScope.async {
            read(url, RssService::readMeta)
        }

        return one.await();
    }

    suspend fun parseNews(url: String): List<News> {
        val one = GlobalScope.async {
            read(url, RssService::readFeed)
        }

        return one.await();
    }

    private fun <T> read(url: String, reader: (parser: XmlPullParser) -> T): T {
        val parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(URL(url).openConnection().getInputStream(), "UTF-8");
        parser.nextTag();

        return reader(parser);
    }

    private fun readMeta(parser: XmlPullParser): String? {

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.name.equals("channel", true)) {
                parser.require(XmlPullParser.START_TAG, null, "channel")
                var title: String? = null;
                var link: String? = null;


                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG)
                        continue;

                    when (parser.name) {
                        "title" -> title = readTitle(parser);
                        "link" -> link = readNewsLink(parser)
                        else -> if (parser.next() == XmlPullParser.TEXT) {
                            parser.nextTag()
                        };
                    }

                }

                return title
            }
        }
        return null;
    }

    private fun readFeed(parser: XmlPullParser): List<News> {
        val news = mutableListOf<News>();

//        parser.require(XmlPullParser.START_TAG, null, "feed")
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
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
                "enclosure" -> imageUrl = readImageLinkEnclosure(parser)
                "content:encoded" -> imageUrl = readImageLinkEncoded(parser)
                else -> if (parser.next() == XmlPullParser.TEXT) {
                    parser.nextTag()
                };
            }

        }

        return News(
            name = title ?: "",
            description = desc ?: "DEFAULT DESCRIPTION LOREM IMPSUM".repeat(10),
            url = link ?: "",
            imageUrl = imageUrl
                ?: "https://cdn.pixabay.com/photo/2013/07/12/12/58/tv-test-pattern-146649__340.png"
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
    private fun readImageLinkEncoded(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "content:encoded")
        val result = readText(parser)
        var link: String? = null;

        var doc = Jsoup.parse(result);
        var image = doc.select("img").first();

        if(image != null)
            link = image.absUrl("src");

        parser.require(XmlPullParser.END_TAG, null, "content:encoded")
        return link
    }

    // Processes summary tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDescription(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "description")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, "description")
        return summary
    }

    // Processes image link tags in enclosure in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readImageLinkEnclosure(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "enclosure")

        var result: String? = null
            if(parser.getAttributeValue(null, "type").contains("image/"))
                result = parser.getAttributeValue(null, "url")
            parser.nextTag()

        parser.require(XmlPullParser.END_TAG, null, "enclosure")
        return result
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