package com.zaaamzomic.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter fun fromPub(v: PublicationStatus): String = v.name
    @TypeConverter fun toPub(v: String): PublicationStatus = try { PublicationStatus.valueOf(v) } catch (_: Exception) { PublicationStatus.UNKNOWN }
    @TypeConverter fun fromReading(v: ReadingStatus?): String? = v?.name
    @TypeConverter fun toReading(v: String?): ReadingStatus? = v?.let { try { ReadingStatus.valueOf(it) } catch (_: Exception) { null } }
}

@Database(entities = [MangaEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ZomicDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
}
