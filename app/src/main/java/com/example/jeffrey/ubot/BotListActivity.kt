package com.example.jeffrey.ubot

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.jeffrey.ubot.model.Bot
import kotlinx.android.synthetic.main.activity_bot_list.*

class BotListActivity : AppCompatActivity() {

    companion object {
        const val ROOM_KEY = "ROOM_KEY"
        private val TAG = BotListActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot_list)

        Log.i(TAG, "Current activity: $TAG")

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        activity_bot_list_btn_u_bot.setOnClickListener {
            showActivity(ChatRoomActivity::class.java, Bot("U Bot", "u-bot", true))
        }

        activity_bot_list_btn_community_bot.setOnClickListener {
            showActivity(ChatRoomActivity::class.java, Bot("Community Bot", "community-bot", false))
        }
    }

    private fun <T: Any> showActivity(activity: Class<T>, bot: Bot) {
        val intent = Intent(this, activity)
        intent.putExtra(ROOM_KEY, bot)
        startActivity(intent)
    }
}
