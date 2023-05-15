package com.example.seyahathanem.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FragmentCategoriesBinding
import com.example.mezunproject.databinding.FragmentNewCategoryBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID


class NewCategoryFragment : Fragment() {

    private var _binding: FragmentNewCategoryBinding? = null
    private val binding get() = _binding!!
    var selectedPicture : Uri? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewCategoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        binding.newCategoryImageView.setOnClickListener {
            newCategoryPicture(it)

        }
        binding.newCategoryCreateButton.setOnClickListener {
            saveToFirebase(binding.newCategoryNameText.text.toString())
            val action = NewCategoryFragmentDirections.actionNewCategoryFragmentToCategoryFragment()
            Navigation.findNavController(it).navigate(action)
        }
    }

    private fun saveToFirebase(categoryName: String){

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val reference = storage.reference
        val imageReference = reference.child("images").child(imageName)

        if (selectedPicture != null) {
            imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                val uploadPictureReference = storage.reference.child("images").child(imageName)
                uploadPictureReference.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString()
                    if (auth.currentUser != null){

                        val hashMap = hashMapOf<String,Any>()
                        hashMap.put("categoryName",categoryName)
                        hashMap.put("downloadUrl",downloadUrl)
                        hashMap.put("date",Timestamp.now())

                        firestore.collection("Users").document(auth.currentUser!!.email.toString()).collection("createdCategories").add(hashMap).addOnSuccessListener {
                            Toast.makeText(requireContext(),"Succeed!",Toast.LENGTH_LONG).show()
                        }

                    }
                }

            }.addOnFailureListener{
                Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun newCategoryPicture(view: View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed to access for gallery!", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }.show()
                }else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }
        }else {

            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permission needed to access for gallery!", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                }else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }

        }
    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null){
                    selectedPicture = intentFromResult.data
                    selectedPicture?.let {
                        binding.newCategoryImageView.setImageURI(it)
                    }
                }


            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->
            if (result){
                //permission granted
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }else {
                //permission denied
                Toast.makeText(requireContext(),"Permission denied!", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}