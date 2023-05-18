package com.example.seyahathanem.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mezunproject.databinding.FragmentSocialBinding
import com.example.seyahathanem.adapters.SocialAdapter
import com.example.seyahathanem.classes.ShareClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class SocialFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var usersAdapter : SocialAdapter
    private lateinit var usersList : ArrayList<ShareClass>

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        usersList = ArrayList<ShareClass>()

        getDataFromFirebase()


        binding.socialRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersAdapter = SocialAdapter(usersList)
        binding.socialRecyclerView.adapter = usersAdapter

        usersList.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataFromFirebase(){


        firestore.collection("Users").addSnapshotListener{value, error->

            if (error != null){
                Toast.makeText(requireContext(),error.message, Toast.LENGTH_LONG).show()
            }else{
                if (value != null && !value.isEmpty){



                    val users = value.documents
                    var pictureUrl : String? = null

                    usersList.clear()

                    for(user in users) {

                        val name = user.get("userName") as String
                        val surname = user.get("userSurname") as String
                        val email = user.get("userEmail") as String

                        if (user.contains("pictureUrl")){
                            pictureUrl = user.get("pictureUrl") as String
                        }






                        val mezun = ShareClass(name,surname,email,pictureUrl)
                        usersList.add(mezun)
                        pictureUrl = null


                    }


                    usersAdapter.notifyDataSetChanged()


                }
            }

        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}