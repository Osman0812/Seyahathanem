package com.example.seyahathanem.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FragmentFriendsBinding
import com.example.mezunproject.databinding.FragmentSocialBinding
import com.example.seyahathanem.adapters.FriendsAdapter
import com.example.seyahathanem.adapters.SocialAdapter
import com.example.seyahathanem.classes.ShareClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FriendsFragment : Fragment() {
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var usersList : ArrayList<ShareClass>
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore
        usersList = ArrayList<ShareClass>()

        getFriendsFromFirebase()

        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendsAdapter = FriendsAdapter(binding.friendsRecyclerView,requireContext(),usersList)
        friendsAdapter.attachSwipeToDelete()
        binding.friendsRecyclerView.adapter = friendsAdapter
    }

    private fun getFriendsFromFirebase() {
        val userFriendsEmails = mutableListOf<String>()

        firestore.collection("Users")
            .document(auth.currentUser!!.email.toString())
            .collection("Friends")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                } else {
                    if (value != null && !value.isEmpty) {
                        val friendsDocuments = value.documents

                        usersList.clear()
                        for (document in friendsDocuments) {
                            val email = document.get("email") as String // 'email' arkadaşın e-postasını içeren alanın adıdır
                            userFriendsEmails.add(email)
                        }

                        // Arkadaşların e-posta adreslerini elde ettik, şimdi bu adresleri kullanarak arkadaşların bilgilerini alabiliriz
                        getFriendsDataFromFirebase(userFriendsEmails)
                    }

                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getFriendsDataFromFirebase(userFriendsEmails: List<String>) {
        val usersList = mutableListOf<ShareClass>()
        var counter = 0

        for (email in userFriendsEmails) {
            firestore.collection("Users").document(email)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.get("userName") as String
                        val surname = document.get("userSurname") as String
                        val email = document.get("userEmail") as String
                        var pictureUrl: String? = null

                        if (document.contains("pictureUrl")) {
                            pictureUrl = document.get("pictureUrl") as String
                        }

                        val userObj = ShareClass(name, surname, email, pictureUrl)
                        usersList.add(userObj)

                        counter++

                        // Only call updateList when all requests have completed
                        if (counter == userFriendsEmails.size) {
                            friendsAdapter.updateList(usersList)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }
    }



    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home){
            requireActivity().onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }


}