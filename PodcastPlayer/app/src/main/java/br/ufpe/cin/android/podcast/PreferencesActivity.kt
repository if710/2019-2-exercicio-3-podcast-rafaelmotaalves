package br.ufpe.cin.android.podcast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequest
import  androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.jetbrains.anko.doAsync
import java.lang.Long.parseLong
import java.util.concurrent.TimeUnit


class PreferencesActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener  {
    private var sharedPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferences_fragment, PreferencesFragment())
            .commit()

        sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }


    override fun onResume() {
        super.onResume()
        sharedPref?.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onStop() {
        super.onStop()
        sharedPref?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, prefKey: String?) {
        if (prefKey == "feed_url") {
            doAsync {
                val db = ItemFeedDB.getDatabase(applicationContext)

                db.itemFeedDAO().deleteAll()
            }
        }

        val repeatIntervalString = sharedPreferences?.getString("update_time", "15") ?: "15"
        val repeatInterval = parseLong(repeatIntervalString, 10)

        val loadFeedRequest = PeriodicWorkRequest.Builder(LoadFeedWorker::class.java, repeatInterval, TimeUnit.MINUTES).build()

        val workManager = WorkManager.getInstance(applicationContext)

        workManager.cancelAllWork()
        workManager.enqueue(loadFeedRequest)
    }

    class PreferencesFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.activity_preferences, rootKey)
        }
    }

}