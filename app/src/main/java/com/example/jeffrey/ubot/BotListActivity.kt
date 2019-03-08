package com.example.jeffrey.ubot

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.jeffrey.ubot.model.Bot
import com.google.firebase.auth.FirebaseAuth
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

        verifyUser()

        setOnClickListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_nav, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_nav_logout -> {
                FirebaseAuth.getInstance().signOut()

                showActivity(LoginActivity::class.java, null)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun verifyUser() {
        val uid = FirebaseAuth.getInstance().uid

        if(uid == null) {
            showActivity(LoginActivity::class.java, null)
        }
    }

    private fun setOnClickListeners() {
        activity_bot_list_btn_u_bot.setOnClickListener {
            showActivity(ChatRoomActivity::class.java, Bot("U Bot", "u-bot", true))
        }

        activity_bot_list_btn_community_bot.setOnClickListener {
            showActivity(ChatRoomActivity::class.java, Bot("Community Bot", "community-bot", false))
        }
    }

    private fun <T: Any> showActivity(activity: Class<T>, bot: Bot?) {
        val intent = Intent(this, activity)

        if(bot != null) {
            intent.putExtra(ROOM_KEY, bot)
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(intent)
    }
}
