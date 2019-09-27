package br.ufpe.cin.android.podcast

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="itemsFeed")
data class ItemFeed (
    val title: String,
    @PrimaryKey val link: String,
    val pubDate: String,
    val description: String,
    val downloadLink: String,
    var downloadPath: String? = null
    ) {

    override fun toString(): String {
        return title
    }
}
