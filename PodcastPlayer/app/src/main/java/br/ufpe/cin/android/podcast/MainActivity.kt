package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {

    private var feedUrl: String? = null
    private var sharedPref: SharedPreferences? = null
    private val FEED_URL: String = "FEED_URL"
    private var feedItems: List<ItemFeed> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = this.getPreferences(Context.MODE_PRIVATE)


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
        feed_items_view.adapter = ItemFeedAdapter(feedItems, this)
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


    internal inner class LoadFeedTask : AsyncTask<Void, Void, List<ItemFeed>>() {

        override fun doInBackground(vararg p0: Void?): List<ItemFeed> {
            var result: List<ItemFeed>

            Log.d ("FeedUrl", feedUrl)
            if (feedUrl != null) {
                var db = ItemFeedDB.getDatabase(applicationContext)

                try {
                    var rssFeedText: String = URL(feedUrl).readText()

                    result = Parser.parse(rssFeedText)

                    var db = ItemFeedDB.getDatabase(applicationContext)

                    db.itemFeedDAO().addItemsFeed(*result.toTypedArray())
                } catch (err : Exception) {
                    Log.d ("FetchFeedError", err.message)
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
