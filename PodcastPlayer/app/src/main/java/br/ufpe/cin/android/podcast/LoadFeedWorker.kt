package br.ufpe.cin.android.podcast

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.Exception
import java.net.URL

class LoadFeedWorker (context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d ("LoadFeedWorker", "Running")
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val feedUrl = sharedPref.getString("feed_url", "")

        try {
            val db = ItemFeedDB.getDatabase(applicationContext)

            var rssFeedText: String = URL(feedUrl).readText()

            val feed = Parser.parse(rssFeedText)

            db.itemFeedDAO().addItemsFeed(*feed.toTypedArray())
        } catch (err : Exception) {
            Log.d("FetchFeedError", err.message)
            return Result.failure()
        }

        return Result.success()
    }

}
