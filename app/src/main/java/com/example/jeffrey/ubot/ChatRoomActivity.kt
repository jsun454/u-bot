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
import kotlin.random.Random

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        private val TAG = ChatRoomActivity::class.java.simpleName

        private const val MARKOV_START_KEY_A = "MARKOV_START_KEY_A"
        private const val MARKOV_START_KEY_B = "MARKOV_START_KEY_B"
        private const val MARKOV_END_KEY = "MARKOV_END_KEY"

        private const val BOT_DELAY: Long = 100
    }

    private val adapter = GroupAdapter<ViewHolder>()
    private val personalMarkovData = HashMap<String, ArrayList<String>>()
    private val sharedMarkovData = HashMap<String, ArrayList<String>>()

    private lateinit var messageListener: ChildEventListener
    private lateinit var personalMarkovListener: ChildEventListener
    private lateinit var sharedMarkovListener: ChildEventListener

    private lateinit var bot: Bot

    private var botActive: Boolean = false
    private var lastBotResponse: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        Log.i(TAG, "Current activity: $TAG")

        setRoomName()
        setMessageListener()
        setMarkovListeners()
        setOnClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()

        removeListeners()
    }

    private fun setRoomName() {
        bot = intent.getParcelableExtra(BotListActivity.ROOM_KEY) ?: return
        supportActionBar?.title = bot.name
    }

    private fun setMessageListener() {
        activity_chat_room_rv_message_list.adapter = adapter

        val user = FirebaseAuth.getInstance().uid ?: return
        val botFile = bot.fileName

        val now = System.currentTimeMillis()
        val ref = FirebaseDatabase.getInstance().getReference("/message-data/$user/$botFile")

        messageListener = object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return

                if(message.senderId == user) {
                    adapter.add(UserMessageItem(message.message))

                    if(message.timeStamp > now) {
                        botActive = true
                        extractMarkovData(message)
                    }
                } else {
                    adapter.add(BotMessageItem(message.message))
                }

                activity_chat_room_rv_message_list.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        }

        ref.addChildEventListener(messageListener)
    }

    private fun setMarkovListeners() {
        val user = FirebaseAuth.getInstance().uid ?: return

        val personalRef = FirebaseDatabase.getInstance().getReference("/markov-data/personal/$user")
        val sharedRef = FirebaseDatabase.getInstance().getReference("/markov-data/shared")

        personalMarkovListener = object: ChildEventListener {
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val valueList = p0.getValue(object: GenericTypeIndicator<ArrayList<String>>() {}) ?: return
                personalMarkovData[p0.ref.key!!] = valueList

                if(botActive) {
                    getBotResponse()
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val valueList = p0.getValue(object: GenericTypeIndicator<ArrayList<String>>() {}) ?: return
                personalMarkovData[p0.ref.key!!] = valueList

                if(botActive) {
                    getBotResponse()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        }

        personalRef.addChildEventListener(personalMarkovListener)

        sharedMarkovListener = object: ChildEventListener {
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
        }

        sharedRef.addChildEventListener(sharedMarkovListener)
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

        val message = Message(user, text, System.currentTimeMillis())

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
        if(System.currentTimeMillis() < lastBotResponse + BOT_DELAY) {
            return
        }

        lastBotResponse = System.currentTimeMillis()

        var wordA = MARKOV_START_KEY_A
        var wordB = MARKOV_START_KEY_B

        val markovChain = ArrayList<String>()

        while(true) {
            val valueList = if(bot.isPersonal) {
                personalMarkovData["$wordA-pair-$wordB"]
            } else {
                sharedMarkovData["$wordA-pair-$wordB"]
            }

            if(valueList == null) {
                lastBotResponse -= BOT_DELAY
                return
            }

            val nextWordIndex = Random.nextInt(valueList.size)
            val nextWord = valueList[nextWordIndex]

            val temp = wordB
            wordB = nextWord
            wordA = temp

            if(nextWord == MARKOV_END_KEY) {
                break
            }

            markovChain.add(wordB)
        }

        val sentence = markovChain.joinToString(" ")

        val user = FirebaseAuth.getInstance().uid ?: return

        val botFile = bot.fileName

        val ref = FirebaseDatabase.getInstance().getReference("/message-data/$user/$botFile").push()

        val botMessage = Message("", sentence, System.currentTimeMillis())

        ref.setValue(botMessage)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully saved bot's response message")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save bot's response message: ${it.message}")
            }
    }

    private fun removeListeners() {
        if(::messageListener.isInitialized) {
            val user = FirebaseAuth.getInstance().uid ?: return

            val botFile = bot.fileName

            val ref = FirebaseDatabase.getInstance().getReference("/message-data/$user/$botFile")

            ref.removeEventListener(messageListener)
        }

        if(::personalMarkovListener.isInitialized) {
            val user = FirebaseAuth.getInstance().uid ?: return

            val ref = FirebaseDatabase.getInstance().getReference("/markov-data/personal/$user")

            ref.removeEventListener(personalMarkovListener)
        }

        if(::sharedMarkovListener.isInitialized) {
            val ref = FirebaseDatabase.getInstance().getReference("/markov-data/shared")

            ref.removeEventListener(sharedMarkovListener)
        }
    }
}