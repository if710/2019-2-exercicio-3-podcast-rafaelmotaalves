package br.ufpe.cin.android.podcast

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="itemsFeed")
data class ItemFeed (
    val title: String,
    @PrimaryKey val link: String,
    val pubDate: String,
    val description: String,
    val downloadLink: String
    ) {

    override fun toString(): String {
        return title
    }
}
