package com.example.notesapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var adapter:AdapterClass
    private lateinit var binding:ActivityMainBinding
    private lateinit var myRef:DatabaseReference
    var isUpdating: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val database =
            Firebase.database("https://notes-app-40288-default-rtdb.asia-southeast1.firebasedatabase.app/")
        binding.menuButton.setOnClickListener{
            showPopup(binding.menuButton)
        }
        val re = Regex("[^A-Za-z\\d ]")
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("mySharedPreference", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val path = re.replace(email.toString(), "")

        myRef = database.getReference(path)
        val dataList: MutableList<NotesData> = emptyList<NotesData>().toMutableList()

        adapter = AdapterClass(this) {
            binding.textToSpeech.shrink()
            isUpdating = it.key
            showOverlays()
            binding.noteInput.setText(it.value)
            binding.saveButton.setOnClickListener { _ ->
                binding.textToSpeech.extend()
                isUpdating = null
                if (binding.noteInput.text.toString().isEmpty()) {
                    myRef.child("Notes").child(it.key).setValue(" ")
                } else {
                    myRef.child("Notes").child(it.key).setValue(binding.noteInput.text.toString())
                }
                hideOverlays()
                binding.noteInput.setText("")
            }
            binding.cancelButton.setOnClickListener {
                binding.textToSpeech.extend()
                isUpdating = null
                hideOverlays()
                binding.noteInput.setText("")
            }
           binding.deleteButton.setOnClickListener {_->
               deleteContent(it.key)
           }
        }
        binding.recyclerView.adapter = adapter
        adapter.submitList(dataList)
        myRef.child("Notes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                snapshot.children.forEach {
                    dataList += (NotesData(it.key.toString(), it.value.toString()))
                    adapter.notifyDataSetChanged()
                }
                Log.e("data", dataList.toString())
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)

        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDateTime.now()
                } else {
                    Log.e("", "")
                }
                val dateTime = current.toString().substring(0, 19)
                Log.e("date", dateTime)
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    binding.inputOverlay.visibility = View.VISIBLE
                    binding.overlay.visibility = View.VISIBLE
                    val res: ArrayList<String> =
                        result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                    var noteKey = re.replace(dateTime, "")
                    if (isUpdating != null) {
                        binding.deleteButton.visibility=View.VISIBLE
                        noteKey = isUpdating!!
                    }

                    binding.noteInput.setText(Objects.requireNonNull(res)[0])
                    binding.saveButton.setOnClickListener {
                        binding.textToSpeech.extend()
                        isUpdating = null
                        if (binding.noteInput.text.toString().isEmpty()) {
                            myRef.child("Notes").child(noteKey).setValue(" ")
                        } else {
                            myRef.child("Notes").child(noteKey)
                                .setValue(binding.noteInput.text.toString())
                        }
                        hideOverlays()
                        binding.noteInput.setText("")
                    }
                    binding.cancelButton.setOnClickListener {
                        binding.textToSpeech.extend()
                        isUpdating = null
                        hideOverlays()
                        binding.noteInput.setText("")
                    }
                }
                else{
                    binding.textToSpeech.extend()
                }
            }
        binding.textToSpeech.setOnClickListener {
            binding.textToSpeech.shrink()
            intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something")
            try {
                activityResultLauncher.launch(intent)
            } catch (e: Exception) {
                binding.textToSpeech.extend()
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
        override fun onBackPressed() {
            if(binding.overlay.visibility == View.VISIBLE){
                binding.textToSpeech.extend()
                binding.overlay.visibility=View.GONE
                binding.deleteButton.visibility=View.GONE
                binding.inputOverlay.visibility=View.GONE
                isUpdating=null
            }
            else{
                onBackPressedDispatcher.onBackPressed()
            }
        }
    private fun showOverlays(){
        binding.deleteButton.visibility=View.VISIBLE
        binding.inputOverlay.visibility = View.VISIBLE
        binding.overlay.visibility = View.VISIBLE
    }
    private fun hideOverlays(){
        binding.deleteButton.visibility=View.GONE
        binding.inputOverlay.visibility = View.GONE
        binding.overlay.visibility = View.GONE
    }


    private fun deleteContent(key: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Do You Really wish to delete this note?")
        builder.setPositiveButton("yes"){ _, _ ->
            myRef.child("Notes").child(key).removeValue()
            binding.textToSpeech.extend()
            adapter.notifyDataSetChanged()
            hideOverlays()
            binding.noteInput.setText("")
        }
        builder.setNegativeButton("No"){ _, _ ->
        }
        builder.show()
    }
    private fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.log_out-> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Log Out")
                    builder.setMessage("Do You Really wish to log out?")
                    builder.setPositiveButton("yes"){ _, _ ->
                        val sharedPreferences: SharedPreferences =
                            getSharedPreferences("mySharedPreference", MODE_PRIVATE)
                        val editor= sharedPreferences.edit()
                        editor.clear().apply()
                        intent= Intent(this,LoginActivity::class.java).apply {
                            flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                    builder.setNegativeButton("No"){ _, _ ->
                    }
                    builder.show()
                }
            }
            true
        }
        popup.show()
    }
}
