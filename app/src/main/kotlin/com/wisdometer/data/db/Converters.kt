package com.wisdometer.data.db

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toInstant(instant: Instant?): Long? = instant?.toEpochMilli()
}
