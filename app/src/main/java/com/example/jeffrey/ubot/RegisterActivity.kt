package com.example.jeffrey.ubot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class RegisterActivity : AppCompatActivity() {

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Log.i(TAG, "Hello")
    }
}