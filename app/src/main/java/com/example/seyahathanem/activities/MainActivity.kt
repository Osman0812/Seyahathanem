package com.example.seyahathanem.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.mezunproject.R
import com.example.mezunproject.databinding.ActivityMainBinding
import com.example.seyahathanem.fragments.CategoriesFragment
import com.example.seyahathanem.fragments.MapsFragment
import com.example.seyahathanem.fragments.SocialFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeFragmentTo(SocialFragment())

        bottomNav = binding.bottomNavigationView.findViewById(R.id.bottomNavigationView)

        bottomNav.selectedItemId = R.id.home


        bottomNav.setOnItemSelectedListener {

            when (it.itemId){
                R.id.home -> {

                    changeFragmentTo(CategoriesFragment())

                }
                R.id.social -> {

                    changeFragmentTo(SocialFragment())

                }
                R.id.map -> {

                    changeFragmentTo(MapsFragment())

                }

            }

            true
        }

    }

    private fun changeFragmentTo(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.frameLayout,fragment).commit()

    }

}