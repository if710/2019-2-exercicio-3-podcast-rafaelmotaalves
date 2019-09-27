package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        if (itemFeed.downloadPath != null) {
            Log.d("RenderItemFeed", itemFeed.toString())
            Log.d ("RenderItemFeed", itemFeed.downloadPath)
        }

        holder.title.text = itemFeed.title
        holder.title.setOnClickListener {
            var intent = Intent (c, EpisodeDetailActivity::class.java)
            intent.putExtra("ITEM_FEED_ID", itemFeed.link)

            c.startActivity(intent)
        }

        holder.downloadButton.setOnClickListener {
            val intent = Intent(c, DownloadService::class.java)
            intent.data = Uri.parse(itemFeed.link)

            c.startService(intent)
        }

        holder.date.text = itemFeed.pubDate
    }


    class ViewHolder (item: View) : RecyclerView.ViewHolder(item) {
        val title: TextView = item.item_title
        val date: TextView = item.item_date
        val downloadButton: Button = item.item_action;
    }


}