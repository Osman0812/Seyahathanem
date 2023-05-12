package com.example.seyahathanem.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mezunproject.R
import com.example.mezunproject.databinding.ActivityCategoryDocumentsBinding
import com.example.seyahathanem.fragments.FeedFragment


class CategoryDocuments : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryDocumentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.add(R.id.frameLayout2,FeedFragment()).commit()

    }
}