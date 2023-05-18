package com.example.seyahathanem.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.mezunproject.databinding.FragmentAddPlaceBinding
import com.example.seyahathanem.activities.MainActivity
import com.example.seyahathanem.viewModel.ImageKeeper
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID


class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    var selectedPicture : Uri? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var categoryName : String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: ImageKeeper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ImageKeeper::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
        sharedPreferences = requireContext().getSharedPreferences("com.example.seyahathanem.fragments",
            Activity.MODE_PRIVATE
        )
        var placeName = requireActivity().intent.getStringExtra("placeName")
        if (placeName != ""){
            binding.placeNameText.setText(placeName)
        }

        placeName = viewModel.placeName
        val imageFromViewModel = viewModel.imageUri
        if (imageFromViewModel != null){
            binding.profilePhoto.setImageURI(imageFromViewModel)
        }
        val comment = viewModel.comment
        if (comment != null){
            binding.commentText.setText(comment)
        }
        val imgCategory = viewModel.selectedImageBitmap
        if (imgCategory!= null){
            binding.categoryPhoto.setImageBitmap(imgCategory)
        }

        //Setting Place When Fragment Recreated
        if (placeName != null){
            binding.placeNameText.setText(placeName)
        }

        binding.profilePhoto.setOnClickListener {

            selectProfilePhoto(it)
        }

        binding.save.setOnClickListener {

            val isOK = saveToFirebase()
            if (isOK == 0){
                val intent = Intent(requireContext(),MainActivity::class.java)
                intent.putExtra("from","fromAddPlaceFragment")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
        binding.categoryPhoto.setOnClickListener {

            imageCategory(it)
            selectCategory(it)

        }

        val data = getDataFromCategory()
        if (data != "null" ){
            val byteArray = android.util.Base64.decode(data,android.util.Base64.DEFAULT)
            setCategoryImage(byteArray)
        }

    }

    private fun setCategoryImage(byteArray: ByteArray){

        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
        val smallBitmap = CategoryFragment().bitmapImage(bitmap,150)
        binding.categoryPhoto.setImageBitmap(smallBitmap)
        //Saving category img in viewModel
        viewModel.selectedCategoryBitmap = smallBitmap

    }

    private fun selectProfilePhoto(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
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

            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
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

    private fun selectCategory(view: View){

        val action = AddPlaceFragmentDirections.actionAddPlaceFragmentToCategoryFragment()
        Navigation.findNavController(view).navigate(action)

    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null){
                    selectedPicture = intentFromResult.data
                    selectedPicture?.let {
                        binding.profilePhoto.setImageURI(it)
                    }
                }


                viewModel.imageUri = selectedPicture

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

    private fun imageCategory(view: View){

        viewModel.placeName = binding.placeNameText.text.toString()

    }

    private fun saveToFirebase(): Int{

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val intent = requireActivity().intent
        var placeName = intent.getStringExtra("placeName")
        val latitude = intent.getDoubleExtra("latitude",0.0)
        val longitude = intent.getDoubleExtra("longitude",0.0)
        val comment = binding.commentText.text.toString()
        val cName = sharedPreferences.getString("cName",null)
        selectedPicture = viewModel.imageUri
        val reference = storage.reference
        if (placeName == null){
            placeName = binding.placeNameText.text.toString()
        }


        val hashMap = hashMapOf<String,Any>()

        if (placeName.isNotEmpty() && cName != "null"){


            hashMap["id"] = imageName
            hashMap["category"] = cName.toString()
            hashMap["placeName"] = placeName
            hashMap["latitude"] = latitude
            hashMap["longitude"] = longitude
            hashMap["comment"] = comment
            hashMap["date"] = Timestamp.now()

            firestore.collection("Users").document(auth.currentUser!!.email.toString()).collection(cName!!).document(imageName).set(hashMap).addOnSuccessListener {

                val ref = firestore.collection("Users").document(auth.currentUser!!.email.toString())

                ref.update("collections",FieldValue.arrayUnion(cName.toString()))
            }
            if (selectedPicture != null){
                val imageReference = reference.child(cName).child(imageName)
                imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                    val uploadPictureReference = storage.reference.child(cName).child(imageName)
                    uploadPictureReference.downloadUrl.addOnSuccessListener {
                        val downloadUrl = it.toString()
                        hashMap["pictureUrl"] = downloadUrl

                        firestore.collection("Users").document(auth.currentUser!!.email.toString()).collection(cName).document(imageName).update(hashMap).addOnSuccessListener {



                        }
                    }
                }
            }

        }else{
            Toast.makeText(requireContext(),"Place name and category sections must be filled !",Toast.LENGTH_LONG).show()
            binding.placeNameText.requestFocus()
            return -1
        }
        return 0


    }


    private fun getDataFromCategory() : String? {

        arguments?.let {
            val data = AddPlaceFragmentArgs.fromBundle(it).category
            categoryName = AddPlaceFragmentArgs.fromBundle(it).categoryName.toString()
            sharedPreferences.edit().putString("cName",categoryName).apply()

            return data
        }
        return ""


    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.comment = binding.commentText.text.toString()

        _binding = null
    }


}