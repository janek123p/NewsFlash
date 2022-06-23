package de.fhac.newsflash.data.service

import android.util.Xml
import android.webkit.URLUtil
import de.fhac.newsflash.data.models.News
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

object RssService {

    /**
     * Parse the meta information of a rss feed. Currently only the feeds title.
     */
    suspend fun parseMeta(url: String): String? {
        val one = GlobalScope.async {
            read(url, RssService::readMeta)
        }

        return one.await();
    }

    /**
     * Parse all news items in the specified rss feed
     */
    suspend fun parseNews(url: String): List<News> {
        val one = GlobalScope.async {
            read(url, RssService::readFeed)
        }

        return one.await();
    }

    /**
     * Connect to the url and read the xml feed
     */
    private fun <T> read(url: String, reader: (parser: XmlPullParser) -> T): T {
        val parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(URL(url).openConnection().getInputStream(), "UTF-8");
        parser.nextTag();


        try{
            parser.require(XmlPullParser.START_TAG, null, "rss");
        }catch(e: Exception){
            throw Exception("Url ist kein RSS-Feed")
        }


        return reader(parser);
    }

    /**
     * Parse the meta information.
     */
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

    /**
     * Parse the news feed
     */
    private fun readFeed(parser: XmlPullParser): List<News> {
        val news = mutableListOf<News>();

//        parser.require(XmlPullParser.START_TAG, null, "feed")
        while (parser.next() != XmlPullParser.END_DOCUMENT) { //Until end of document
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue;
            }

            //Check if current tag is an item (news) tag
            if (parser.name.equals("item", true)) {
                news.add(readEntry(parser)) //Read entry
            }
        }

        return news;
    }

    /**
     * Read a single entry.
     */
    private fun readEntry(parser: XmlPullParser): News {
        parser.require(XmlPullParser.START_TAG, null, "item")
        var title: String? = null;
        var desc: String? = null;
        var link: String? = null;
        var imageUrl: String? = null;
        var date: Date? = null;


        while (parser.next() != XmlPullParser.END_TAG) { //Read until entry ended
            if (parser.eventType != XmlPullParser.START_TAG)
                continue;

            when (parser.name) { //Parse known feed properties
                "title" -> title = readTitle(parser);
                "description" -> desc = readDescription(parser);
                "link" -> link = readNewsLink(parser)
                "enclosure" -> imageUrl = imageUrl ?: readImageLinkEnclosure(parser) //Image type one
                "content:encoded" -> imageUrl = imageUrl ?: readImageLinkEncoded(parser)//Image type two
                "media:group" -> imageUrl = imageUrl ?: readImageLinkGroup(parser) //Image type three
                "pubDate" -> date = readPubdate(parser)
                else -> if (parser.next() == XmlPullParser.TEXT) {
                    parser.nextTag()
                };
            }

        }

        return News(
            title = title ?: "No title",
            description = desc ?: "",
            url = link ?: "",
            pubDate = date ?: Date(),
            imageUrl = imageUrl
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

    // Processes image link tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readImageLinkGroup(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "media:group")

        var result: String? = null

        while (parser.next() != XmlPullParser.END_TAG) { //Read until entry ended
            if (parser.eventType != XmlPullParser.START_TAG)
                continue;

            if(parser.name == "media:content"){
                if(parser.getAttributeValue(null, "medium").contains("image") && result == null)
                    result = parser.getAttributeValue(null, "url")

                parser.nextTag()
            }
        }



        parser.require(XmlPullParser.END_TAG, null, "media:group")
        return result
    }

    // Processes description tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDescription(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "description")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, "description")
        return summary
    }

    // Processes summary tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readPubdate(parser: XmlPullParser): Date {
        parser.require(XmlPullParser.START_TAG, null, "pubDate")
        val summary = readText(parser)

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        val date = format.parse(summary);


        parser.require(XmlPullParser.END_TAG, null, "pubDate")
        return date;
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