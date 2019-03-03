package com.example.jeffrey.ubot

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_bot_list.*

class BotListActivity : AppCompatActivity() {

    companion object {
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
            showActivity(ChatRoomActivity::class.java)
        }

        activity_bot_list_btn_community_bot.setOnClickListener {

        }
    }

    private fun <T: Any> showActivity(activity: Class<T>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }
}
