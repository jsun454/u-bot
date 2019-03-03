package com.example.jeffrey.ubot

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Log.i(TAG, "Current activity: $TAG")

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        activity_register_btn_register.setOnClickListener {
            registerUser()
        }

        activity_register_txt_go_login.setOnClickListener {
            showLoginActivity()
        }
    }

    private fun registerUser() {
        val email = activity_register_et_email.text.toString()
        val password = activity_register_et_password.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email/password", Toast.LENGTH_SHORT).show()
            return
        }

        activity_register_btn_register.isClickable = false

        Toast.makeText(this, "Creating user...", Toast.LENGTH_SHORT).show()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully created user: ${it.user.uid}")
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to create user: ${it.message}")
                Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()

                activity_register_btn_register.isClickable = true
            }
    }

    private fun showLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        finish()
    }
}