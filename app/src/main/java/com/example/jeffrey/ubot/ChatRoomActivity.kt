package com.example.jeffrey.ubot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.jeffrey.ubot.model.Bot
import com.example.jeffrey.ubot.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chat_room.*

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        private val TAG = ChatRoomActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        Log.i(TAG, "Current activity: $TAG")

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        activity_chat_room_btn_send.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val text = activity_chat_room_et_user_message.text.toString()

        if(text.isEmpty()) {
            return
        }

        val user = FirebaseAuth.getInstance().uid ?: return

        val bot = intent.getParcelableExtra<Bot>(BotListActivity.ROOM_KEY)
        val botFile = bot.fileName

        val userRef = FirebaseDatabase.getInstance().getReference("/personal-messages/$user/$botFile").push()
        val communityRef = FirebaseDatabase.getInstance().getReference("/community-messages/").push()

        val message = Message(userRef.key!!, user, text, System.currentTimeMillis())

        userRef.setValue(message)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully saved message")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save message: ${it.message}")
            }

        communityRef.setValue(message)
    }
}
