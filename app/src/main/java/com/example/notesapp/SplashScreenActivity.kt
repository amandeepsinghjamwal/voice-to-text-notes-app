package com.example.notesapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val intent=Intent(this,MainActivity::class.java).apply {
            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val intent2=Intent(this,LoginActivity::class.java).apply {
            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val sharedPreferences: SharedPreferences = getSharedPreferences("mySharedPreference", MODE_PRIVATE)
        val email=sharedPreferences.getString("email",null)
        CoroutineScope(Dispatchers.Default).launch {
            delay(1500)
            if(email!=null){
                startActivity(intent)
            }
            else{
                startActivity(intent2)
            }
        }
    }
}