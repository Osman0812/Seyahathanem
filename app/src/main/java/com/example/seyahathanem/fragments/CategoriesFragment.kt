package com.example.seyahathanem.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FragmentCategoriesBinding
import com.example.seyahathanem.adapters.GridRVAdapter
import com.example.seyahathanem.viewModel.DataModal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var gridView: GridView
    private var dataModalArrayList: ArrayList<DataModal> = ArrayList()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter : GridRVAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridView = requireActivity().findViewById(R.id.idGRV)
        firestore = Firebase.firestore
        auth = Firebase.auth

        getDataFromFirebase()

        adapter = GridRVAdapter(dataModalArrayList,requireContext())
        gridView.adapter = adapter



    }

    private fun getDataFromFirebase() {


        firestore.collection("Users").document(auth.currentUser!!.email.toString())
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                } else {
                    if (value != null) {
                        val collections = value.get("collections") as ArrayList<*>

                        dataModalArrayList.clear()
                        for (collection in collections) {
                            //getDataFromFirebase(collection)
                            val data = DataModal(collection.toString())
                            dataModalArrayList.add(data)



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