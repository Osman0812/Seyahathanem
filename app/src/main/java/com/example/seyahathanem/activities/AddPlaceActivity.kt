package com.example.seyahathanem.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.mezunproject.R
import com.example.mezunproject.databinding.ActivityAddPlaceBinding

class AddPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}