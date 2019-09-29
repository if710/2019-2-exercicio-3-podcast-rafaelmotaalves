package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemlista.view.*

class ItemFeedAdapter (private val feedItems: List<ItemFeed>, private val c: Context, private val podcastPlayerService: PodcastPlayerService?) : RecyclerView.Adapter<ItemFeedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(c).inflate(R.layout.itemlista, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = feedItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var itemFeed = feedItems[position]

        val hasDownloadedEpisode = itemFeed.downloadPath != null

        val playIcon = Icon.createWithResource(c, R.drawable.play)
        val downloadIcon = Icon.createWithResource(c, R.drawable.download)

        if (hasDownloadedEpisode) {
            holder.actionButton.setImageIcon(playIcon)
        } else {
            holder.actionButton.setImageIcon(downloadIcon)
        }


        holder.title.text = itemFeed.title
        holder.title.setOnClickListener {
            var intent = Intent (c, EpisodeDetailActivity::class.java)
            intent.putExtra("ITEM_FEED_ID", itemFeed.link)

            c.startActivity(intent)
        }

        holder.actionButton.setOnClickListener {
            if (!hasDownloadedEpisode) {
                val intent = Intent(c, DownloadService::class.java)
                intent.data = Uri.parse(itemFeed.link)

                c.startService(intent)
            } else {
                podcastPlayerService?.toggle(itemFeed)
            }
        }

        holder.date.text = itemFeed.pubDate
    }


    class ViewHolder (item: View) : RecyclerView.ViewHolder(item) {
        val title: TextView = item.item_title
        val date: TextView = item.item_date
        val actionButton: ImageButton = item.item_action
    }


}