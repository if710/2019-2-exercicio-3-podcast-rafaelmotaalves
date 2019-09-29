package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : IntentService("DownloadService") {
    override fun onHandleIntent(i: Intent?) {
        val itemFeedLink = i?.data?.toString()

        if (itemFeedLink != null) {
            val db = ItemFeedDB.getDatabase(applicationContext)

            val itemFeed = db.itemFeedDAO().findByLink(itemFeedLink).first()

            val downloadUri = Uri.parse(itemFeed.downloadLink)

            val root = getExternalFilesDir(DIRECTORY_DOWNLOADS)
            root?.mkdirs()

            val output = File(root, downloadUri.lastPathSegment!!)
            if (output.exists()) {
                output.delete()
            }
            val url = URL(i.data!!.toString())
            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try {
                val `in` = c.inputStream
                val buffer = ByteArray(8192)
                var len = `in`.read(buffer)
                while (len >= 0) {
                    Log.d ("EpisodeDownload", "Downloading " + output.path)
                    out.write(buffer, 0, len)
                    len = `in`.read(buffer)
                }
                out.flush()
                Log.d ("EpisodeDownload", "Download finished " + output.path)

                itemFeed.downloadPath = output.path

                db.itemFeedDAO().updateItemsFeed(itemFeed)

                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(DOWNLOAD_FINISHED))
            } finally {
                fos.fd.sync()
                out.close()
                c.disconnect()
            }

        }
    }
}
