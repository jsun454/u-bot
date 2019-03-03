package com.example.jeffrey.ubot

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.i(TAG, "Current activity: $TAG")

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        activity_login_btn_login.setOnClickListener {
            loginUser()
        }

        activity_login_txt_go_register.setOnClickListener {
            showActivity(RegisterActivity::class.java)
        }
    }

    private fun loginUser() {
        val email = activity_login_et_email.text.toString()
        val password = activity_login_et_password.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email/password", Toast.LENGTH_SHORT).show()
            return
        }

        activity_login_btn_login.isClickable = false

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully logged in as user: ${it.user.uid}")
                showActivity(BotListActivity::class.java)
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to log in as user: ${it.message}")
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()

                activity_login_btn_login.isClickable = true
            }
    }

    private fun <T: Any> showActivity(activity: Class<T>) {
        val intent = Intent(this, activity)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        finish()
    }
}
