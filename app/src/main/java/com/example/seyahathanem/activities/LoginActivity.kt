package com.example.seyahathanem.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.mezunproject.R
import com.example.mezunproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = Firebase.auth


        if (auth.currentUser != null){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }




    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home){
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}