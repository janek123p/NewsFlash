package de.fhac.newsflash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import de.fhac.newsflash.data.models.News

class NewsListAdapter(
    private val context: Context,
    private val data: List<News>
) : BaseAdapter() {

    
    override fun getCount(): Int {
        return data.count()
    }

    override fun getItem(p0: Int): Any {
        return data[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var convertView = inflater.inflate(de.fhac.newsflash.R.layout.news_card, null)
        val news = data[position]

        convertView.setOnClickListener(View.OnClickListener {
            (context as MainActivity).showDetailedNews(news);
        })

        return convertView.apply {
            if (news.imageUrl != null){
                Glide.with(context).load(news.imageUrl).into(findViewById(R.id.thumbnail));
            }

            findViewById<TextView>(de.fhac.newsflash.R.id.news_title)
                .setText(news.name)
            findViewById<TextView>(de.fhac.newsflash.R.id.news_content)
                .setText(news.description)
        }
    }
}