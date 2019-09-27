package br.ufpe.cin.android.podcast

import androidx.room.*

@Dao
interface ItemFeedDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addItemsFeed (vararg itemsFeed: ItemFeed)

    @Update()
    fun updateItemsFeed (vararg itemsFeed: ItemFeed)

    @Query("SELECT * FROM itemsFeed WHERE link=:itemLink")
    fun findByLink (itemLink: String): Array<ItemFeed>

    @Query("SELECT * FROM itemsFeed")
    fun allFeedItems (): Array<ItemFeed>

    @Query("DELETE FROM itemsFeed")
    fun deleteAll ()
}