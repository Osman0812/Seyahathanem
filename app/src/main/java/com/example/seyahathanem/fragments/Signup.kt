package com.example.seyahathanem.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mezunproject.databinding.FragmentSignupBinding
import com.example.seyahathanem.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class Signup : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var email : String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage


    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage


        binding.createAccount.setOnClickListener {

            val ok = createUser()
            if (ok != -1){
                uploadToStorage()
            }

        }


    }

    private fun createUser():Int{

        if (binding.signupEmailText.text.isEmpty() || binding.passwordText.text.isEmpty()){
            Toast.makeText(context,"Enter Email and Password!", Toast.LENGTH_LONG).show()
            return -1
        }else{
            auth.createUserWithEmailAndPassword(binding.signupEmailText.text.toString(), binding.passwordText.text.toString()).addOnSuccessListener {

                val intent = Intent(context, MainActivity::class.java)
                Toast.makeText(context,"Account Created!",Toast.LENGTH_LONG).show()
                startActivity(intent)
                activity?.finish()

            }.addOnFailureListener {
                Toast.makeText(context,it.message, Toast.LENGTH_LONG).show()
            }
            return 0


        }



    }
    private fun uploadToStorage(){

        email = binding.signupEmailText.text.toString()
        val hashMap = hashMapOf<String, Any>()
        hashMap["userEmail"] = binding.signupEmailText.text.toString()
        hashMap["userName"] = binding.nameText.text.toString()
        hashMap["userSurname"] = binding.surnameText.text.toString()
        hashMap["password"] = binding.passwordText.text.toString()



        firestore.collection("Users").document(email).set(hashMap).addOnSuccessListener {
            Toast.makeText(context,"Account Created!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(context,it.message, Toast.LENGTH_LONG).show()
        }

    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}