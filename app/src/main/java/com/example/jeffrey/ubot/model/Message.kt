package com.example.jeffrey.ubot.model

class Message(val senderId: String?, val message: String, val timeStamp: Long) {
    constructor(): this("", "", -1)
}