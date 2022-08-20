package com.daotrung.myapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.daotrung.myapplication.model.*

@Database(
    entities = [MusicLocal::class, Album::class, Artist::class, QueueItem::class, VideoLocal::class, PrivateVideoPath::class, VideoContinue::class, PlayListEntity::class,PlaylistWithVideoMusic::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(DataConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistsDao(): ArtistDao
    abstract fun albumsDao(): AlbumDao
    abstract fun songsDao(): SongDao
    abstract fun queueDao(): QueueItemsDao
    abstract fun videoRecent(): RecentVideoDao
    abstract fun privateVideoDao(): PrivateVideoDao
    abstract fun videoContinue(): VideoContinueDao
    abstract fun playListDao(): PlayListDao
    abstract fun playListVideoOrMusic():PlayListVideoMusicDao

}
