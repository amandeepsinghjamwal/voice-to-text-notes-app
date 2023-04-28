package com.example.notesapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.notesapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth=Firebase.auth
        binding.signupButton.setOnClickListener{
            startActivity(Intent(this,SignupActivity::class.java))
        }
        binding.resetPassword.setOnClickListener{
            if(binding.loginEmail.text!!.isNotBlank()){
                auth.sendPasswordResetEmail(binding.loginEmail.text.toString())
                Toast.makeText(this,"Reset password mail sent successfully",Toast.LENGTH_SHORT).show()
                binding.resetPassword.isEnabled=false
            }
            else{
                binding.loginEmail.error="Please enter your email"
            }

        }
        binding.loginButton.setOnClickListener{
            if(checkInvalidConditions(binding.loginEmail.text.toString(),binding.loginPassword.text.toString())) {
                auth.signInWithEmailAndPassword(
                    binding.loginEmail.text.toString(),
                    binding.loginPassword.text.toString()
                ).addOnCompleteListener(this)
                { task ->
                    if (task.isSuccessful) {
                        val sharedPreferences: SharedPreferences =
                            getSharedPreferences("mySharedPreference", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("email", binding.loginEmail.text.toString()).apply()
                        val intent=Intent(Intent(this, MainActivity::class.java)).apply {
                            flags= Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    private fun checkInvalidConditions(email:String, password:String):Boolean {
        if(email.isBlank()||password.isBlank()){
            if(email.isBlank()){
                binding.loginEmail.error="Please fill this field"
                return false
            }
            else if(password.isBlank()){
                binding.loginPassword.error="Please fill this field"
                return false}
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.loginPassword.error="Enter a valid email id"
            return false
        }
        return true
    }
}