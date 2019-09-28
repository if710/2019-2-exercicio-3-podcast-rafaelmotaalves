package br.ufpe.cin.android.podcast

import android.app.*
import android.content.Intent
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


class PodcastPlayerService : Service() {
    private var mPlayer: MediaPlayer? = null

    private val mBinder = PodcastBinder()

    var currentItemFeed : ItemFeed? = null

    override fun onCreate() {
        super.onCreate()
        Log.d ("PodcastPlayerService", "Creating")
        mPlayer = MediaPlayer()

        mPlayer?.isLooping = true

        createChannel()

        val notificationIntent = Intent(applicationContext, PodcastPlayerService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(
            applicationContext,"1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true).setContentTitle("PodcastPlayer rodando")
            .setContentText("Clique para acessar o player!")
            .setContentIntent(pendingIntent).build()

        startForeground(NOTIFICATION_ID, notification)
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        saveCurrentProgress(currentItemFeed)

        mPlayer?.release()
        super.onDestroy()
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
                Log.d ("SavedPosition", currPos.toString())

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

    override fun onBind(intent: Intent): IBinder {
        return mBinder
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
    }

}