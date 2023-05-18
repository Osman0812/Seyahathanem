package com.example.seyahathanem.activities

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mezunproject.R
import com.example.mezunproject.databinding.ActivityProfileBinding
import com.example.seyahathanem.classes.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    var selectedPicture : Uri? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var name : String? = null
    private var surname : String? = null
    private var email : String? = null
    private var selectedItem : String? = null
    private var countryName : String? = null
    private var jobFirma : String? = null
    private var cityName : String? = null
    private var phoneNumber : String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var ez : Int? = null
    private lateinit var user : User
    private val hashMap = hashMapOf<String, Any>()
    private var docName : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = this.getSharedPreferences("com.example.mezunproject.activities", MODE_PRIVATE)

        registerLauncher()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firestore = Firebase.firestore
        storage = Firebase.storage
        auth = Firebase.auth




        val intentFromMain = intent
        val info = intent.getStringExtra("info")
        if (info.equals("old")){ // Just looking up for profiles
            binding.saveButton.visibility = View.INVISIBLE
            val selectedUser = intent.getStringExtra("email")
            getFinalDataFromFirebase(selectedUser.toString())


        }else{ // Editing Profile

            docName = auth.currentUser!!.email.toString()


            //Education


            binding.saveButton.setOnClickListener {
                getData()
                uploadProfilePhoto(email.toString(),hashMap)
                saveToFirebase()
                getFinalDataFromFirebase(docName!!)
                sharedPreferences.edit().putBoolean("isFirst",false).apply() // after filling profile

                val intent = Intent(this,MainActivity::class.java)
                intent.putExtra("fromProfile",true)
                //intent.putExtra("name",shareClass.name).putExtra("surname",shareClass.surname).putExtra("email",shareClass.email)
                startActivity(intent)
                finish()
            }



            getFinalDataFromFirebase(docName!!)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home){
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun uploadProfilePhoto( imagePath : String, hashMap: HashMap<String,Any>){

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val reference = storage.reference
        val imageReference = reference.child("Users").child(imagePath)

        if (selectedPicture != null){

            imageReference.putFile(selectedPicture!!).addOnSuccessListener{

                val uploadPictureReference = storage.reference.child("Users").child(imagePath)
                uploadPictureReference.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString()
                    hashMap["pictureUrl"] = downloadUrl

                    firestore.collection("Users").document(imagePath).update(hashMap)

                }


            }.addOnFailureListener{
                Toast.makeText(this,it.message, Toast.LENGTH_LONG).show()
            }

        }

    }

    fun selectProfilePhoto(view: View){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){
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

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
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
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null){
                    selectedPicture = intentFromResult.data
                    selectedPicture?.let {
                        binding.imageView.setImageURI(it)
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
                Toast.makeText(this,"Permission denied!",Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun getFinalDataFromFirebase(docName : String){
        var profilePictureString : String? = null


        firestore.collection("Users").document(docName).addSnapshotListener{value, error ->
            if (error != null){
                Toast.makeText(this,error.message,Toast.LENGTH_LONG).show()
            }else{

                val position = sharedPreferences.getInt("position",0)



                name = value?.get("userName") as String
                surname = value.get("userSurname") as String
                email = value.get("userEmail") as String
                if (value.contains("country")){
                    countryName = value.get("country") as String
                }
                if (value.contains("city")){
                    cityName = value.get("city") as String
                }
                if (value.contains("job")){
                    jobFirma = value.get("job") as String
                }
                if (value.contains("phone")){
                    phoneNumber = value.get("phone") as String
                }
                if (value.contains("pictureUrl")){
                    profilePictureString = value.get("pictureUrl") as String
                }


                binding.profileNameText.setText(name)
                binding.profileSurnameText.setText(surname)
                binding.profileEmailText.setText(email)
                if (countryName?.isNotEmpty() == true){
                    binding.jobCountry.setText(countryName)
                }
                if (cityName?.isNotEmpty() == true){
                    binding.jobCity.setText(cityName)
                }
                if (jobFirma?.isNotEmpty() == true){
                    binding.jobFirma.setText(jobFirma)
                }
                if (phoneNumber?.isNotEmpty() == true){
                    binding.profilePhoneText.setText(phoneNumber)
                }
                if (profilePictureString != null){
                    Picasso.get().load(profilePictureString).into(binding.imageView)
                    //binding.imageView.setImageURI(selectedPicture)
                }else{
                    Picasso.get().load(R.drawable.profilephoto).into(binding.imageView)
                }


                user = User(name!!,surname!!,email!!,selectedItem,countryName,phoneNumber,cityName,jobFirma)



            }
        }

    }

    private fun getData(){

        name = binding.profileNameText.text.toString()
        surname = binding.profileSurnameText.text.toString()
        email = binding.profileEmailText.text.toString()
        countryName = binding.jobCountry.text.toString()
        cityName = binding.jobCity.text.toString()
        jobFirma = binding.jobFirma.text.toString()

        phoneNumber = binding.profilePhoneText.text.toString()

        user = User(name!!,surname!!,email!!,selectedItem,countryName,phoneNumber,cityName,jobFirma)
    }
    private fun saveToFirebase(){

        hashMap["userName"] = binding.profileNameText.text.toString()
        hashMap["userSurname"] = binding.profileSurnameText.text.toString()
        hashMap["country"] = user.jobCountry.toString()
        hashMap["city"] = user.jobCity.toString()
        hashMap["job"] = user.jobFirma.toString()
        hashMap["phone"] = user.phoneNumber.toString()


        firestore.collection("Users").document(user.email).update(hashMap).let {
            it.addOnSuccessListener {
                Toast.makeText(applicationContext,"Succeed!",Toast.LENGTH_LONG).show()

            }.addOnFailureListener {error ->
                Toast.makeText(applicationContext,error.message,Toast.LENGTH_LONG).show()
            }
        }

    }


}