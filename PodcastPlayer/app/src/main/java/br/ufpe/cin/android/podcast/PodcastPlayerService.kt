package br.ufpe.cin.android.podcast

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.doAsync
import java.io.File
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T


class PodcastPlayerService : Service() {

    private var mPlayer: MediaPlayer? = null
    private val mBinder = PodcastBinder()
    var currentItemFeed : ItemFeed? = null

    override fun onCreate() {
        super.onCreate()

        mPlayer = MediaPlayer()
        mPlayer?.isLooping = true

        startForegroundNotification()
    }

    private fun startForegroundNotification () {
        createChannel()

        val notificationIntent = Intent(applicationContext, PodcastPlayerService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val playIntent = Intent()
        playIntent.action = PLAY_ACTION
        val playPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, playIntent, 0)

        val notification = NotificationCompat.Builder(
            applicationContext,"1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true).setContentTitle("PodcastPlayer rodando em segundo plano")
            .setContentText("Clique para acessar o player!")
            .addAction(NotificationCompat.Action(R.drawable.play, getString(R.string.action_play), playPendingIntent))
            .setContentIntent(pendingIntent).build()

        startForeground(NOTIFICATION_ID, notification)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        registerBroadcastReceiver()

        return START_STICKY
    }

    override fun onDestroy() {
        saveCurrentProgress(currentItemFeed)

        mPlayer?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    fun toggle(itemFeed: ItemFeed) {
        if (itemFeed != currentItemFeed) {
            handlePodcastChange(itemFeed)

            play()
        } else {
            if (!mPlayer!!.isPlaying) {
                play()
            } else {
                pause()
            }
        }
    }

    private fun handlePodcastChange(itemFeed: ItemFeed) {
        val podcastFile = File(itemFeed.downloadPath)

        saveCurrentProgress(currentItemFeed)

        mPlayer?.reset()
        mPlayer?.setDataSource(applicationContext, Uri.fromFile(podcastFile))
        mPlayer?.prepare()
        mPlayer?.seekTo(itemFeed.currentPosition ?: 0)


        currentItemFeed = itemFeed
    }

    private fun saveCurrentProgress (itemFeed: ItemFeed?) {
        if (currentItemFeed != null) {
            val currPos = mPlayer?.currentPosition

            itemFeed?.currentPosition = currPos

            doAsync {
                val db = ItemFeedDB.getDatabase(applicationContext)

                db.itemFeedDAO().updateItemsFeed(itemFeed!!)
            }
        }
    }

    fun play () {
        if (!mPlayer!!.isPlaying) {
            mPlayer?.start()
        }
    }

    fun pause() {
        if (mPlayer!!.isPlaying) {
            mPlayer?.pause()
        }
    }

    inner class PodcastBinder : Binder() {
        internal val service: PodcastPlayerService
            get() = this@PodcastPlayerService
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && currentItemFeed != null) {
                Log.d ("PodcastReceiver", action)

                if (action == PLAY_ACTION) {
                    Log.d ("PodcastReceiver", PLAY_ACTION)
                    toggle(currentItemFeed!!)
                }
            }
        }
    }

    private fun registerBroadcastReceiver () {
        val intentFilter = IntentFilter()
        intentFilter.addAction(PLAY_ACTION)
        registerReceiver(receiver, intentFilter)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel("1", "Canal de Notificacoes", NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.description = "Descricao"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
    companion object {
        private const val NOTIFICATION_ID = 2
        private const val PLAY_ACTION = "br.ufpe.cin.android.podcast.PlayAction"
    }

}