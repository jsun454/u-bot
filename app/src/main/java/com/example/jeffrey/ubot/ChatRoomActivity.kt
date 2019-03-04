package com.example.jeffrey.ubot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.jeffrey.ubot.model.Bot
import com.example.jeffrey.ubot.model.Message
import com.example.jeffrey.ubot.view.BotMessageItem
import com.example.jeffrey.ubot.view.UserMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_room.*

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        private val TAG = ChatRoomActivity::class.java.simpleName
        private const val MARKOV_START_KEY_A = "MARKOV_START_KEY_A"
        private const val MARKOV_START_KEY_B = "MARKOV_START_KEY_B"
        private const val MARKOV_END_KEY = "MARKOV_END_KEY"
    }

    private val adapter = GroupAdapter<ViewHolder>()
    private val personalMarkovData = HashMap<String, ArrayList<String>>()
    private val sharedMarkovData = HashMap<String, ArrayList<String>>()
    private lateinit var bot: Bot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        Log.i(TAG, "Current activity: $TAG")

        setRoomName()
        setMessageListener()
        setMarkovListeners()
        setOnClickListener()
    }

    private fun setRoomName() {
        bot = intent.getParcelableExtra(BotListActivity.ROOM_KEY) ?: return
        supportActionBar?.title = bot.name
    }

    private fun setMessageListener() {
        activity_chat_room_rv_message_list.adapter = adapter

        val user = FirebaseAuth.getInstance().uid ?: return
        val botFile = bot.fileName

        val ref = FirebaseDatabase.getInstance().getReference("/message-data/$user/$botFile")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return

                if(message.senderId == user) {
                    adapter.add(UserMessageItem(message.message))
                    extractMarkovData(message)
                    getBotResponse()
                } else {
                    adapter.add(BotMessageItem(message.message))
                }

                activity_chat_room_rv_message_list.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun setMarkovListeners() {
        val user = FirebaseAuth.getInstance().uid ?: return
        val personalRef = FirebaseDatabase.getInstance().getReference("/markov-data/personal/$user")
        val sharedRef = FirebaseDatabase.getInstance().getReference("/markov-data/shared/")

        personalRef.addChildEventListener(object: ChildEventListener {
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val valueList = p0.getValue(object: GenericTypeIndicator<ArrayList<String>>() {}) ?: return
                personalMarkovData[p0.ref.key!!] = valueList
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val valueList = p0.getValue(object: GenericTypeIndicator<ArrayList<String>>() {}) ?: return
                personalMarkovData[p0.ref.key!!] = valueList
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })

        sharedRef.addChildEventListener(object: ChildEventListener {
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val valueList = p0.getValue(object: GenericTypeIndicator<ArrayList<String>>() {}) ?: return
                sharedMarkovData[p0.ref.key!!] = valueList
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val valueList = p0.getValue(object: GenericTypeIndicator<ArrayList<String>>() {}) ?: return
                sharedMarkovData[p0.ref.key!!] = valueList
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
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
        val botFile = bot.fileName

        val ref = FirebaseDatabase.getInstance().getReference("/message-data/$user/$botFile").push()

        val message = Message(ref.key!!, user, text, System.currentTimeMillis())

        ref.setValue(message)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully saved message")
                activity_chat_room_et_user_message.text.clear()
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save message: ${it.message}")
            }

    }

    private fun extractMarkovData(message: Message) {
        val wordList = ArrayList<String>()
        wordList.add(MARKOV_START_KEY_A)
        wordList.add(MARKOV_START_KEY_B)
        wordList.addAll(message.message.trim().splitToSequence(' ')
            .filter {
                it.isNotEmpty()
            }
            .toList())
        wordList.add(MARKOV_END_KEY)

        if(wordList.count() == 3) {
            return
        }

        val user = FirebaseAuth.getInstance().uid ?: return

        for(i in 0..wordList.count() - 3) {
            val markovKey = "${wordList[i]}-pair-${wordList[i+1]}"
            val markovValue = wordList[i+2]

            val personalRef = FirebaseDatabase.getInstance().getReference("/markov-data/personal/$user/$markovKey")
            val sharedRef = FirebaseDatabase.getInstance().getReference("/markov-data/shared/$markovKey")

            personalRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    Log.d("JEFFREY", "I AM RUNNING")
                    val personalValueList = ArrayList<String>()
                    personalValueList.addAll(p0.getValue(object: GenericTypeIndicator<ArrayList<String>>() {})
                        ?: arrayListOf())
                    personalValueList.add(markovValue)
                    personalRef.setValue(personalValueList)
                }

                override fun onCancelled(p0: DatabaseError) {}
            })

            sharedRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val sharedValueList = ArrayList<String>()
                    sharedValueList.addAll(p0.getValue(object: GenericTypeIndicator<ArrayList<String>>() {})
                        ?: arrayListOf())
                    sharedValueList.add(markovValue)
                    sharedRef.setValue(sharedValueList)
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }
    }

    private fun getBotResponse() {
        // TODO: fetch response from bot
    }
}