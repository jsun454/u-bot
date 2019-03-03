package com.example.jeffrey.ubot.view

import com.example.jeffrey.ubot.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.partial_bot_message.view.*
import kotlinx.android.synthetic.main.partial_user_message.view.*

class UserMessageItem(private val message: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.partial_user_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.partial_user_message_btn_message.text = message
    }
}

class BotMessageItem(private val message: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.partial_bot_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.partial_bot_message_btn_message.text = message
    }
}