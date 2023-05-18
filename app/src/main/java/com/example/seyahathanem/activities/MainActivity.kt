package com.example.seyahathanem.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.mezunproject.R
import com.example.mezunproject.databinding.ActivityMainBinding
import com.example.seyahathanem.fragments.AddPlaceFragmentArgs
import com.example.seyahathanem.fragments.CategoriesFragment
import com.example.seyahathanem.fragments.MapsFragment
import com.example.seyahathanem.fragments.SocialFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var bottomNav : BottomNavigationView
    private lateinit var firestore : FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        changeFragmentTo(CategoriesFragment())

        bottomNav = binding.bottomNavigationView.findViewById(R.id.bottomNavigationView)

        bottomNav.selectedItemId = R.id.home

        auth = Firebase.auth
        firestore = Firebase.firestore

        // From add place done successfully
        val from = intent.getStringExtra("from")
        if (from.equals("fromAddPlaceFragment")){

            val fragmentManager = supportFragmentManager
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            Toast.makeText(applicationContext,"Saved Successfully!", Toast.LENGTH_LONG).show()
        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_items,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.profile){
            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)

        }
        if (item.itemId == R.id.logout){
            auth.signOut()
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        return super.onOptionsItemSelected(item)
    }


    private fun changeFragmentTo(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.frameLayout,fragment).commit()

    }

}