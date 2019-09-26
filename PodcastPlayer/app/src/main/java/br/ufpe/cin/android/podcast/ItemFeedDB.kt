package br.ufpe.cin.android.podcast

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ItemFeed::class], version = 1)
abstract  class ItemFeedDB : RoomDatabase() {
    abstract fun itemFeedDAO(): ItemFeedDAO

    companion object {
        private var INSTANCE: ItemFeedDB? = null
        fun getDatabase(ctx: Context) : ItemFeedDB {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        ctx.applicationContext,
                        ItemFeedDB::class.java,
                        "itemFeed.db"
                    ).build()
                }

            }
            return INSTANCE!!
        }


    }
}