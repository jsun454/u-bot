package com.example.jeffrey.ubot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Bot(val name: String, val fileName: String, val isPersonal: Boolean): Parcelable {
    constructor(): this("", "", true)
}