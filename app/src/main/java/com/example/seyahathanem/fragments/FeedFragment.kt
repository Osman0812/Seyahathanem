package com.example.seyahathanem.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FragmentCategoryBinding
import com.example.mezunproject.databinding.FragmentFeedBinding
import com.example.seyahathanem.adapters.FeedAdapter
import com.example.seyahathanem.classes.FeedScreenClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var categoryList : ArrayList<FeedScreenClass>
    private lateinit var categories : ArrayList<*>
    private lateinit var adapter : FeedAdapter
    private lateinit var cNames : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore

        categoryList = ArrayList()
        cNames = ArrayList()

        //categories = getCategoriesFromFirebase()


        val cName = requireActivity().intent.getStringExtra("cName")

        getCategoriesDataFromFirebase(cName.toString())

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FeedAdapter(categoryList)
        binding.recyclerView.adapter = adapter





    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getCategoriesDataFromFirebase(collection : String){

        firestore.collection("Users").document(auth.currentUser!!.email.toString()).collection(collection).addSnapshotListener { value, error ->

            if (error != null){
                Toast.makeText(requireContext(),error.message,Toast.LENGTH_LONG).show()
            }else{
                if (value != null && !value.isEmpty){
                    val documents = value.documents

                    //categoryList.clear()
                    for (document in documents){
                        var categoryImage : String ? = null

                        val categoryName = document.get("placeName") as String
                        if (document.contains("pictureUrl")){
                            categoryImage = document.get("pictureUrl") as String
                        }

                        val place = FeedScreenClass(categoryImage,categoryName)
                        categoryList.add(place)

                    }
                    adapter.notifyDataSetChanged()


                }

            }

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getCategoriesFromFirebase() {


        firestore.collection("Users").document(auth.currentUser!!.email.toString()).addSnapshotListener { value, error ->

            if (error != null){
                Toast.makeText(requireContext(),error.message,Toast.LENGTH_LONG).show()
            }else {


                if (value != null && value.contains("collections")){

                    categories = value.get("collections") as ArrayList<*>

                    cNames = ArrayList<String>(categories.map { it.toString() })
                    for (category in cNames){
                        println(categoryList.size)

                        getCategoriesDataFromFirebase(category)






                    }
                    adapter.notifyDataSetChanged()

                }
            }

        }




    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}