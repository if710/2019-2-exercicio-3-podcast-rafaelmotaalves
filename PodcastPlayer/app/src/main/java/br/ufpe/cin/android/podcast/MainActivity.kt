package br.ufpe.cin.android.podcast

import android.content.*
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val FEED_URL: String = "FEED_URL"

    private var feedUrl: String? = null
    private var sharedPref: SharedPreferences? = null
    private var feedItems: List<ItemFeed> = emptyList()

    internal var podcastPlayerService: PodcastPlayerService? = null
    internal var isBound = false

    private val sConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            podcastPlayerService = null
            isBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, b: IBinder?) {
            val binder = b as PodcastPlayerService.PodcastBinder
            podcastPlayerService = binder.service
            isBound = true

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = this.getPreferences(Context.MODE_PRIVATE)

        val podcastPlayerIntent = Intent(this, PodcastPlayerService::class.java)
        startService(podcastPlayerIntent)

        feed_items_view.layoutManager = LinearLayoutManager(this)

        feed_items_view.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        feedUrl = getFeedUrl() ?: ""
        feed_link_input.setText(feedUrl)
        LoadFeedTask().execute()

        feed_link_submit.setOnClickListener {
            DeleteFeedTask().execute()
            if (sharedPref != null) {
                val editor = sharedPref?.edit()
                editor?.putString(FEED_URL, feed_link_input.text.toString())
                editor?.apply()
            }
            feedUrl = getFeedUrl()
            LoadFeedTask().execute()
        }
    }

    private fun getFeedUrl (): String? {
        return sharedPref?.getString(FEED_URL, null)
    }

    fun renderFeedItems () {
        feed_items_view.adapter = ItemFeedAdapter(feedItems, this, podcastPlayerService)
    }

    internal inner class DeleteFeedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {

            var db = ItemFeedDB.getDatabase(applicationContext)

            db.itemFeedDAO().deleteAll()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            feedItems = emptyList()
        }

    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val bindIntent = Intent(this,PodcastPlayerService::class.java)
            isBound = bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE)
            Log.d ("PodcastPlayerBind", isBound.toString())

        }
    }

    override fun onStop() {
        unbindService(sConn)
        super.onStop()
    }


    internal inner class LoadFeedTask : AsyncTask<Void, Void, List<ItemFeed>>() {

        override fun doInBackground(vararg p0: Void?): List<ItemFeed> {
            var result: List<ItemFeed>

            Log.d ("FeedUrl", feedUrl)
            if (feedUrl != null) {
                var db = ItemFeedDB.getDatabase(applicationContext)

                try {
                    var rssFeedText: String = URL(feedUrl).readText()

                    val feed = Parser.parse(rssFeedText)

                    var db = ItemFeedDB.getDatabase(applicationContext)

                    db.itemFeedDAO().addItemsFeed(*feed.toTypedArray())
                } catch (err : Exception) {
                    Log.d ("FetchFeedError", err.message)
                } finally {
                    result = db.itemFeedDAO().allFeedItems().toList()
                }
            } else {
                result = emptyList()
            }

            return result
        }

        override fun onPostExecute(result: List<ItemFeed>?) {
            super.onPostExecute(result)

            if (result != null) {
                feedItems = result
                renderFeedItems()
            }

        }

    }

}
