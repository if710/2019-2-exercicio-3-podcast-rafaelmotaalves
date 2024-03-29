package br.ufpe.cin.android.podcast

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager.*
import kotlinx.android.synthetic.main.activity_main.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity() {

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


    private val receiver = object : BroadcastReceiver() {

        val intentFilter: IntentFilter
            get() {
                val intentFilter = IntentFilter()
                intentFilter.addAction(UPDATE_FEED)
                intentFilter.addAction(UPDATE_DOWNLOAD)
                return intentFilter
            }

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d ("MainActivityReceiver", action)
            if (action == UPDATE_FEED || action == UPDATE_DOWNLOAD) {
                loadAndRenderFeedItems()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, receiver.intentFilter)

        loadAndRenderFeedItems()

        val podcastPlayerIntent = Intent(this, PodcastPlayerService::class.java)
        startService(podcastPlayerIntent)

        setContentView(R.layout.activity_main)

        sharedPref = getDefaultSharedPreferences(this)

        feed_items_view.layoutManager = LinearLayoutManager(this)

        feed_items_view.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        config_activity.setOnClickListener {
            val intent = Intent(applicationContext, PreferencesActivity::class.java)

            startActivity(intent)
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)

        super.onDestroy()
    }

    private fun loadAndRenderFeedItems () {
        doAsync {
            val db = ItemFeedDB.getDatabase(applicationContext)

            feedItems = db.itemFeedDAO().allFeedItems().toList()

            uiThread {
                renderFeedItems()
            }
        }
    }


    private fun renderFeedItems () {
        feed_items_view.adapter = ItemFeedAdapter(feedItems, this, podcastPlayerService)
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val bindIntent = Intent(this,PodcastPlayerService::class.java)
            isBound = bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        unbindService(sConn)
        isBound = false
        super.onStop()
    }

}
