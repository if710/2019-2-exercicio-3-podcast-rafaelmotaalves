package br.ufpe.cin.android.podcast

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_episode_detail.*

class EpisodeDetailActivity : AppCompatActivity() {

    private var itemFeedId : String? = null
    private var itemFeed: ItemFeed? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemFeedId = intent.extras?.getString("ITEM_FEED_ID")
        LoadItemFeedTask().execute()

        setContentView(R.layout.activity_episode_detail)
    }

    fun renderScreen () {
        Log.d ("ItemFeedDetail", itemFeed.toString())

        item_title.text = itemFeed?.title ?: ""
        description.text = itemFeed?.description ?: ""
        link.text = itemFeed?.link ?: ""
    }


    internal inner class  LoadItemFeedTask : AsyncTask<Void, Void, ItemFeed>() {
        override fun doInBackground(vararg p0: Void?): ItemFeed {
            var db = ItemFeedDB.getDatabase(applicationContext)

            return db.itemFeedDAO().findByLink(itemFeedId!!).first()
        }

        override fun onPostExecute(result: ItemFeed?) {
            super.onPostExecute(result)

            if (result != null) {
                itemFeed = result
                renderScreen()
            }
        }
    }

}
