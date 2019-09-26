package br.ufpe.cin.android.podcast

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ItemFeedDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addItemsFeed (vararg itemsFeed: ItemFeed)

    @Query("SELECT * FROM itemsFeed WHERE link=:itemLink")
    fun findByLink (itemLink: String): Array<ItemFeed>

    @Query("SELECT * FROM itemsFeed")
    fun allFeedItems (): Array<ItemFeed>

    @Query("DELETE FROM itemsFeed")
    fun deleteAll ()
}