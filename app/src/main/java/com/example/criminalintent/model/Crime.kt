package com.example.criminalintent.model

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.Companion.INTEGER
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    @Embedded var suspect: Suspect? = null
) {
    val photoFileName get() = "IMG_$id.jpg"
}

data class Suspect(var suspectName: String? = null, var phoneNumber: String? = null)