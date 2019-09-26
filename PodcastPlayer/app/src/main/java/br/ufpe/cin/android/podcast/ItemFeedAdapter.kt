package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemlista.view.*

class ItemFeedAdapter (private val feedItems: List<ItemFeed>, private val c: Context) : RecyclerView.Adapter<ItemFeedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(c).inflate(R.layout.itemlista, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = feedItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var itemFeed = feedItems[position]

        holder.title.text = itemFeed.title
        holder.title.setOnClickListener {
            var intent = Intent (c, EpisodeDetailActivity::class.java)
            intent.putExtra("ITEM_FEED_ID", itemFeed.link)

            c.startActivity(intent)
        }

        holder.date.text = itemFeed.pubDate
    }


    class ViewHolder (item: View) : RecyclerView.ViewHolder(item) {
        val title: TextView = item.item_title
        val date: TextView = item.item_date
        
    }


}