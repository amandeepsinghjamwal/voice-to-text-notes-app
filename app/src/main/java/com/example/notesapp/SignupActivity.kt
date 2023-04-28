package com.example.notesapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.notesapp.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern
import kotlin.random.Random

class SignupActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignupBinding
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivitySignupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth= Firebase.auth
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        binding.signupButton.setOnClickListener{
            if(checkInvalidConditions(binding.signupName.text.toString(),binding.signupEmail.text.toString())){
            val signupPassword=getRandPassword(10)
            auth.createUserWithEmailAndPassword(binding.signupEmail.text.toString(),signupPassword).addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    builder.setMessage("Your account has been successfully created and mail to set your password has been sent to ${binding.signupEmail.text.toString()}")
                    auth.sendPasswordResetEmail(binding.signupEmail.text.toString())
                    builder.setPositiveButton("Ok"){ _, _ ->
                        finish()
                    }
                    builder.setCancelable(false)
                    builder.show()
                }
                else{
                    Toast.makeText(this,task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                }
            }
            }
        }
    }
    private fun checkInvalidConditions(name:String, email:String):Boolean {
        if(email.isBlank()||name.isBlank()){

            if(name.isBlank()){
                binding.signupName.error="Please fill this field"
                return false}
            else if(email.isBlank()){
                binding.signupEmail.error="Please fill this field"
                return false
            }
        }
        if(name.length>20){
            binding.signupName.error="Name cannot be more than 20 characters"
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.signupEmail.error="Please enter valid email"
            return false
        }
        return true
    }
    private fun getRandPassword(n: Int): String
    {
        val characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val random = Random(System.nanoTime())
        val password = StringBuilder()

        for (i in 0 until n)
        {
            val rIndex = random.nextInt(characterSet.length)
            password.append(characterSet[rIndex])
        }

        return password.toString()
    }
}