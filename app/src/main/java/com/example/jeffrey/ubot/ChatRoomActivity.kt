package com.example.jeffrey.ubot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.jeffrey.ubot.model.Bot
import com.example.jeffrey.ubot.model.Message
import com.example.jeffrey.ubot.view.BotMessageItem
import com.example.jeffrey.ubot.view.UserMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_room.*

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        private val TAG = ChatRoomActivity::class.java.simpleName
    }

    private val adapter = GroupAdapter<ViewHolder>()
    private var bot: Bot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        Log.i(TAG, "Current activity: $TAG")

        setRoomName()
        setMessageListener()
        setOnClickListener()
    }

    private fun setRoomName() {
        bot = intent.getParcelableExtra(BotListActivity.ROOM_KEY)
        supportActionBar?.title = bot?.name ?: "Bot"
    }

    private fun setMessageListener() {
        activity_chat_room_rv_message_list.adapter = adapter

        val user = FirebaseAuth.getInstance().uid ?: return
        val botFile = bot?.fileName ?: return

        val ref = FirebaseDatabase.getInstance().getReference("/personal-messages/$user/$botFile")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return
                if(message.senderId == user) {
                    adapter.add(UserMessageItem(message.message))
                } else {
                    adapter.add(BotMessageItem(message.message))
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun setOnClickListener() {
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
        val botFile = bot?.fileName ?: return

        val userRef = FirebaseDatabase.getInstance().getReference("/personal-messages/$user/$botFile").push()
        val communityRef = FirebaseDatabase.getInstance().getReference("/community-messages/").push()

        val message = Message(userRef.key!!, user, text, System.currentTimeMillis())

        userRef.setValue(message)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully saved message")
                activity_chat_room_et_user_message.text.clear()
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save message: ${it.message}")
            }

        communityRef.setValue(message)
    }
}
