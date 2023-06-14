package com.example.seyahathanem.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FragmentMapsBinding
import com.example.seyahathanem.activities.AddPlaceActivity
import com.example.seyahathanem.classes.IconClass
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.Calendar


class MapsFragment : Fragment() , OnMapReadyCallback, OnMapLongClickListener {


    private lateinit var auth: FirebaseAuth
    private lateinit var firestore : FirebaseFirestore

    private var _binding: FragmentMapsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var resultLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean : Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var positionsList: ArrayList<LatLng>
    private lateinit var icons: IconClass
    private lateinit var iconsList: ArrayList<IconClass>
    // initialize this with actual mapping of friend's emails to names
    val friendsMap: HashMap<String, String> = hashMapOf("friend1@example.com" to "Friend 1", "friend2@example.com" to "Friend 2")
    var selectedFriends: BooleanArray = BooleanArray(friendsMap.size) { false }  // initially none selected
    private var rectangle: Polygon? = null
    private var southWest: LatLng? = null
    private var northEast: LatLng? = null
    var buttonClicked = false
    var spatialQueryJob: Job? = null






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            showCategoryDialog()
        }

        binding.cancel.visibility = View.INVISIBLE
        Places.initialize(requireContext(),"AIzaSyCofjcj7zf41IWDl6DoeL9UzV-BYyR0Gd8")

        iconsList = ArrayList()
        positionsList = ArrayList()
        permissionLauncher()
        auth = Firebase.auth
        firestore = Firebase.firestore

        sharedPreferences = activity?.getSharedPreferences("com.example.seyahathanem.fragments",
            MODE_PRIVATE)!!

        trackBoolean = false

        selectedLatitude = 0.0
        selectedLongitude = 0.0

        val friendsButton = view.findViewById<ImageButton>(R.id.friends_button)
        friendsButton.setOnClickListener {
            // Load the friends selection dialog
            openFriendsSelectionDialog()
        }



    }



    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.setOnMapLongClickListener(this)

        // From see selectedPlace Location
        val mLatitude = arguments?.getDouble(ARG_LATITUDE) ?: 0.0
        val mLongitude = arguments?.getDouble(ARG_LONGITUDE) ?: 0.0
        val mLocationLatLng = LatLng(mLatitude, mLongitude)




        // Up to here
        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                if (trackBoolean == false) {
                    val userLocation = LatLng(location.latitude,location.longitude)
                    println(userLocation.latitude.toString())
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17f))
                    sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                }

            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root,"Need perm",Snackbar.LENGTH_INDEFINITE).setAction("Give"){

                }.show()
            }else {
                resultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }



        }else{
            //permission granted
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)


            //Control where we came from
            if (mLongitude != 0.0 && mLatitude != 0.0){

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocationLatLng, DEFAULT_ZOOM))
            }else{
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,17f))

                }
            }
            mMap.isMyLocationEnabled = true
        }

        mMap.uiSettings.isZoomControlsEnabled = true


        displayAllIcons()
        //getLocations(false)

        //mySharedLocations()


    }
    private suspend fun spatialQuery() = suspendCancellableCoroutine<Unit>{ cont->
        binding.cancel.setOnClickListener {
            mMap.clear()
            binding.cancel.visibility = View.INVISIBLE
            spatialQueryJob?.cancel()

            // Replace the current fragment with a new instance of it
            parentFragmentManager.beginTransaction().replace(
                R.id.fragmentMaps,  // The id of the FrameLayout where your fragment is displayed
                MapsFragment()  // A new instance of your fragment
            ).commit()
        }
        buttonClicked = true
        mMap.setOnMapClickListener { point ->
            // Clear the map
            mMap.clear()

            if (southWest == null) {
                // If southWest is null, this is the first click, so we set southWest to the point
                southWest = point


            } else if (northEast == null) {
                // If southWest is not null but northEast is, this is the second click, so we set northEast to the point
                northEast = point

                // Remove the old rectangle
                rectangle?.remove()

                // Draw a rectangle with southWest and northEast as corners
                rectangle = mMap.addPolygon(
                    PolygonOptions()
                        .add(southWest, LatLng(southWest!!.latitude, northEast!!.longitude), northEast, LatLng(northEast!!.latitude, southWest!!.longitude))
                        .strokeColor(Color.RED)
                )

                AlertDialog.Builder(requireContext())
                    .setTitle("Spatial Query")
                    .setMessage("Kendi verilerinizi mi yoksa arkadaşlarınızın verilerini mi sorgulamak istersiniz?")
                    .setPositiveButton("Kendi verilerim") { _, _ ->
                        displayUsersFilterDialog()
                        //displayAllIcons(southWest = southWest, northEast = northEast)
                    }
                    .setNegativeButton("Arkadaşlarımın verileri") { _, _ ->
                        openFriendsSelectionDialog()
                    }
                    .show()


            } else {
                // If both southWest and northEast are not null, the user is starting a new rectangle, so we clear everything
                southWest = null
                northEast = null
                rectangle?.remove()
                rectangle = null

                // Refresh the markers
                //displayAllIcons()
            }
        }
    }

    private fun showCategoryDialog() {
        // Define the categories
        val categories = arrayOf("Restaurants", "Entertainment", "Shopping", "Cars")

        // Create an AlertDialog Builder
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select a Category")

        // Set a single choice items list for alert dialog
        builder.setSingleChoiceItems(categories, -1) { dialog, which ->
            val selectedCategory = categories[which]

            // Call your method to filter markers by the selected category
            mMap.clear()
            displayAllIcons(selectedCategory)

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.maps_menu,menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.filter){
            filterAlert()

        }
        if (item.itemId == R.id.spatial_query){
            mMap.clear()
            binding.cancel.visibility = View.VISIBLE
            spatialQueryJob?.cancel()
            spatialQueryJob = CoroutineScope(Dispatchers.Main).launch {
                spatialQuery()
            }
        }

        return super.onOptionsItemSelected(item)
    }
    private fun filterAlert(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("How many days' posts would you like to see?")
            .setMessage("Days between 1 and 30: ")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("OK") {_,_ ->
            val userInput = input.text.toString()
            val number = userInput.toIntOrNull()

            if (number != null && number in 1..30){

                mMap.clear()
                fetchFriendLocations(true,number,null)

            }else{
                Toast.makeText(requireContext(),"Invalid input!",Toast.LENGTH_LONG).show()
            }

        }
        builder.setNegativeButton("Cancel"){it,_->
            it.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun openFriendsSelectionDialog() {
        val currentUserEmail = auth.currentUser!!.email.toString()

        firestore.collection("Users")
            .document(currentUserEmail)
            .collection("Friends")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val friendsList = querySnapshot.documents.map { it["email"] as String }
                displayFriendsSelectionDialog(friendsList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to fetch friends: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayFriendsSelectionDialog(friendsList: List<String>) {
        val selectedFriends = BooleanArray(friendsList.size)
        val friendsArray = friendsList.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select friends")
            .setMultiChoiceItems(friendsArray, selectedFriends) { _, which, isChecked ->
                selectedFriends[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                mMap.clear()
                val selectedFriendsEmails = friendsList.filterIndexed { index, _ -> selectedFriends[index] }
                displayFilterDialog(selectedFriendsEmails)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun displayUsersFilterDialog() {
        val filterOptions = arrayOf("Last 5 days", "Last 10 days", "Last 15 days", "All posts")
        var selectedOption = 0

        AlertDialog.Builder(requireContext())
            .setTitle("Select filter option")
            .setSingleChoiceItems(filterOptions, selectedOption) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("OK") { _, _ ->
                val days: Int? = when (selectedOption) {
                    0 -> 5
                    1 -> 10
                    2 -> 15
                    else -> null
                }
                if (days == null){
                    displayAllIcons(days)
                }else{
                    displayAllIcons(null,null,null,days)
                }

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun displayFilterDialog(selectedFriends: List<String>) {
        val filterOptions = arrayOf("Last 5 days", "Last 10 days", "Last 15 days", "All posts")
        var selectedOption = 0

        AlertDialog.Builder(requireContext())
            .setTitle("Select filter option")
            .setSingleChoiceItems(filterOptions, selectedOption) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("OK") { _, _ ->
                val days: Int? = when (selectedOption) {
                    0 -> 5
                    1 -> 10
                    2 -> 15
                    else -> null
                }
                if (days == null){
                    fetchFriendLocations(false,days, selectedFriends)
                }else{
                    fetchFriendLocations(true,days, selectedFriends)
                }

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun displayAllIcons(type: String? = null, southWest: LatLng? = null, northEast: LatLng? = null, days: Int? = null) {
        val ref = firestore.collection("Users").document(auth.currentUser!!.email.toString())
        ref.addSnapshotListener { value, error ->
            if (value != null) {
                if (value.contains("collections")) {
                    val collections = value.get("collections") as ArrayList<*>

                    for (collection in collections) {
                        var query: Query = ref.collection(collection.toString())
                        if (type != null){
                            query = query.whereEqualTo("category", type)
                        }

                        // If days is not null, filter documents by date
                        if (days != null) {
                            val startDate = Calendar.getInstance()
                            startDate.add(Calendar.DAY_OF_MONTH, -days)
                            query = query.whereGreaterThanOrEqualTo("date", startDate.time)
                        }

                        query.addSnapshotListener { v, e ->
                            if (v != null) {
                                val sharedData = v.documents
                                for (shared in sharedData) {
                                    val latitude = shared.get("latitude") as Double
                                    val longitude = shared.get("longitude") as Double
                                    val latLng = LatLng(latitude, longitude)
                                    val name = shared.get("placeName") as String
                                    val comment = shared.get("comment") as String

                                    // Check if this location is within the selected area
                                    if (southWest != null && northEast != null) {
                                        if (latitude !in southWest.latitude..northEast.latitude || longitude !in southWest.longitude..northEast.longitude) {
                                            continue
                                        }
                                    }

                                    val markerOptions = MarkerOptions().position(latLng).title(name).snippet(comment)

                                    if (shared.contains("pictureUrl")) {
                                        val picture = shared.get("pictureUrl") as String
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val bitmapDescriptor = getBitmapDescriptorFromUrl(picture,100)
                                            withContext(Dispatchers.Main) {
                                                if (bitmapDescriptor != null) {
                                                    markerOptions.icon(bitmapDescriptor)
                                                }
                                                mMap.addMarker(markerOptions)
                                            }
                                        }
                                    } else {
                                        mMap.addMarker(markerOptions)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private suspend fun getBitmapDescriptorFromUrl(url: String, desiredSize: Int): BitmapDescriptor? {
        return try {
            val bitmap = Picasso.get().load(url).get()
            val circularBitmap = getCircularBitmap(bitmap, desiredSize)
            BitmapDescriptorFactory.fromBitmap(circularBitmap)
        } catch (e: Exception) {
            null
        }
    }



    private fun fetchFriendLocations(filter: Boolean, days: Int?, selectedFriendsEmails: List<String>?) {
        val userRef = firestore.collection("Users")
        userRef.document(auth.currentUser!!.email.toString()).collection("Friends").get()
            .addOnSuccessListener { friends ->
                for (friend in friends.documents) {
                    val friendEmail = friend.get("email") as String
                    // If selectedFriendsEmails is null or friendEmail is in selectedFriendsEmails, then fetch data
                    if (selectedFriendsEmails == null || friendEmail in selectedFriendsEmails) {
                        userRef.document(friendEmail).get()
                            .addOnSuccessListener { user ->
                                if (user.contains("collections")) {
                                    val collections = user.get("collections") as ArrayList<*>
                                    for (collection in collections) {
                                        userRef.document(friendEmail).collection(collection.toString()).get()
                                            .addOnSuccessListener { docs ->
                                                for (doc in docs.documents) {
                                                    if (days != null) {
                                                        val startDate = Calendar.getInstance()
                                                        startDate.add(Calendar.DAY_OF_MONTH, -days)
                                                        val query = userRef.document(friendEmail).collection(collection.toString()).whereGreaterThanOrEqualTo("date",startDate.time)
                                                        query.get().addOnSuccessListener { snapshot->
                                                            for (doc in snapshot) {
                                                                addMarker(doc, friendEmail)
                                                            }
                                                        }
                                                    } else {
                                                        userRef.document(friendEmail).collection(collection.toString()).get().addOnSuccessListener { snapshot->
                                                            for (doc in snapshot) {
                                                                addMarker(doc, friendEmail)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
            }
    }
    private fun addMarker(doc: DocumentSnapshot, friendName: String) {
        val latitude = doc.get("latitude") as Double
        val longitude = doc.get("longitude") as Double
        val latLng = LatLng(latitude, longitude)
        val name = doc.get("placeName") as String
        val com = doc.get("comment") as String
        val markerTitle = "$name (Shared by $friendName)"
        val markerOptions = MarkerOptions().position(latLng).title(markerTitle).snippet(com)

        if (doc.contains("pictureUrl")) {
            val picture = doc.get("pictureUrl") as String
            CoroutineScope(Dispatchers.IO).launch {
                val bitmapDescriptor = getBitmapDescriptorFromUrl(picture,100)
                withContext(Dispatchers.Main) {
                    if (bitmapDescriptor != null) {
                        markerOptions.icon(bitmapDescriptor)
                    }
                    mMap.addMarker(markerOptions)
                }
            }
        }else{
            mMap.addMarker(markerOptions)
        }


    }


/*
    private fun getLocations(filter: Boolean, days: Int?= null){

        val ref = firestore.collection("Users")
        ref.document(auth.currentUser!!.email.toString()).collection("Friends").addSnapshotListener { value, error ->
            if (error != null){
                Toast.makeText(requireContext(),error.message,Toast.LENGTH_LONG).show()
            }else{
                if(value != null && !value.isEmpty){

                    val friends = value.documents
                    for (friend in friends){
                        ref.addSnapshotListener { v, e ->
                            if (v != null && !v.isEmpty){
                                val users = v.documents
                                for (user in users){
                                    val usersEmail = user.get("userEmail") as String
                                    val friendsEmail = friend.get("email") as String

                                    if (usersEmail == friendsEmail){
                                        //friend found
                                        if (user.contains("collections")){
                                            val collections = user.get("collections") as ArrayList<*>
                                            for (collection in collections) {
                                                //which request check
                                                if (filter) {
                                                    val startDate = Calendar.getInstance()
                                                    startDate.add(Calendar.DAY_OF_MONTH, -days!!)

                                                    val query = ref.document(friendsEmail).collection(collection.toString()).whereGreaterThanOrEqualTo("date",startDate.time)
                                                    query.get()
                                                        .addOnSuccessListener {snapshot->
                                                            for (doc in snapshot){
                                                                var picture: String? = null
                                                                val latitude = doc.get("latitude") as Double
                                                                val longitude = doc.get("longitude") as Double
                                                                val latLng = LatLng(latitude, longitude)
                                                                val name = doc.get("placeName") as String
                                                                val com = doc.get("comment") as String
                                                                val markerOptions = MarkerOptions().position(latLng)
                                                                if (doc.contains("pictureUrl")) {
                                                                    picture = doc.get("pictureUrl") as String
                                                                    getCircleIcon(picture, Color.WHITE, Color.BLACK) { bitmap ->

                                                                        if (bitmap != null) {
                                                                            val iconBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                                                                            markerOptions.icon(iconBitmapDescriptor)
                                                                        }

                                                                    }
                                                                }

                                                                markerOptions.title(name).snippet(com)
                                                                mMap.addMarker(markerOptions)


                                                            }

                                                        }

                                                } else{
                                                    ref.document(friendsEmail)
                                                        .collection(collection.toString())
                                                        .addSnapshotListener { cNed, error ->
                                                            if (cNed != null) {
                                                                val sharedData = cNed.documents
                                                                for (shared in sharedData) {
                                                                    var picture: String? = null

                                                                    val date = shared.get("date") as Date
                                                                    val latitude = shared.get("latitude") as Double
                                                                    val longitude = shared.get("longitude") as Double
                                                                    val latLng = LatLng(latitude, longitude)
                                                                    val name = shared.get("placeName") as String
                                                                    val com = shared.get("comment") as String
                                                                    val markerOptions = MarkerOptions().position(latLng)
                                                                    if (shared.contains("pictureUrl")) {
                                                                        picture = shared.get("pictureUrl") as String
                                                                        getCircleIcon(picture!!, Color.WHITE, Color.BLACK) { bitmap ->

                                                                            if (bitmap != null) {
                                                                                val iconBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                                                                                markerOptions.icon(iconBitmapDescriptor)
                                                                            }

                                                                        }
                                                                    }

                                                                    markerOptions.title(name).snippet(com)
                                                                    mMap.addMarker(markerOptions)



                                                                }


                                                            }
                                                        }

                                                }
                                            }
                                        }else{
                                            break
                                            //friend published nothing
                                        }

                                    }

                                }
                            }

                        }
                    }




                }else{
                    //Arkadasi yok
                }
            }
        }





    }




 */

    private fun getCircleIcon(imageUrl: String, fillColor: Int, strokeColor: Int, callback: (Bitmap?) -> Unit) {
        val radius = 40 // Adjust the desired circle radius in pixels
        val strokeWidth = 4 // Adjust the desired stroke width in pixels

        // Create a shape drawable with a circle shape
        val shapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.paint.color = fillColor
        shapeDrawable.paint.style = Paint.Style.FILL
        shapeDrawable.paint.isAntiAlias = true

        // Create a stroke paint for the circle
        val strokePaint = Paint()
        strokePaint.color = strokeColor
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = strokeWidth.toFloat()

        // Load the image from the URL using Picasso
        Picasso.get().load(imageUrl).into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    // Crop the loaded image into a circular shape
                    val croppedBitmap = getCircularBitmap(bitmap,100)

                    // Create a bitmap and canvas to draw the circle icon
                    val iconBitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(iconBitmap)
                    canvas.drawCircle(radius.toFloat(), radius.toFloat(), (radius - strokeWidth).toFloat(), shapeDrawable.paint)
                    canvas.drawCircle(radius.toFloat(), radius.toFloat(), (radius - strokeWidth).toFloat(), strokePaint)

                    // Draw the cropped image onto the circle icon
                    val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, radius * 2, radius * 2, true)
                    val iconCanvas = Canvas(iconBitmap)
                    iconCanvas.drawBitmap(scaledBitmap, 0f, 0f, null)

                    // Return the final circle icon bitmap
                    callback(iconBitmap)
                } else {
                    // Failed to load the image, return null
                    callback(null)
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                // Failed to load the image, return null
                callback(null)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        })
    }

    private fun getCircularBitmap(bitmap: Bitmap, desiredSize: Int): Bitmap {
        val output = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        paint.isAntiAlias = true
        val rect = Rect(0, 0, desiredSize, desiredSize)
        val radius = desiredSize / 2f
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, desiredSize, desiredSize, true)
        val shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        canvas.drawCircle(radius, radius, radius, paint)
        return output
    }

    private fun permissionLauncher(){

        resultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result){
                //access granted
                if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)

                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null){
                        val lastKnownLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation,15f))
                    }
                    mMap.isMyLocationEnabled = true
                }


            }else{
                //access denied
                Toast.makeText(context,"Permission Denied!", Toast.LENGTH_LONG).show()
            }
        }

    }


    override fun onMapLongClick(p0: LatLng) {

        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude

        var place = ""



        lifecycleScope.launch(Dispatchers.IO){
            val placesClient = Places.createClient(requireContext())


            withContext(Dispatchers.Main){
                val placeFields = listOf(com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)
                val placeRequest = FindCurrentPlaceRequest.newInstance(placeFields)

                if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    placesClient.findCurrentPlace(placeRequest).addOnSuccessListener { response ->

                        val selectedLocationLatLng = Location(LocationManager.GPS_PROVIDER).apply {

                            latitude = p0.latitude
                            longitude = p0.longitude

                        }
                        val filteredPlaces = response.placeLikelihoods.filter {placeLikelihood ->

                            val placeLocation = placeLikelihood.place.latLng
                            if (placeLocation != null){

                                val placeLatLng = Location(LocationManager.GPS_PROVIDER).apply {
                                    latitude = placeLocation.latitude
                                    longitude = placeLocation.longitude
                                }
                                placeLatLng.distanceTo(selectedLocationLatLng) < 10

                            }else {
                                false
                            }

                        }
                        val placeNames = filteredPlaces.map { placeLikelihood ->
                            placeLikelihood.place.name
                        }

                        if (placeNames.isNotEmpty()){

                            place = placeNames[0]!!.toString()



                        }
                        alert(place, selectedLatitude!!, selectedLongitude!!)

                    }.addOnFailureListener {
                        Toast.makeText(requireContext(),"Failed to retrieve current place: ${it.message}",Toast.LENGTH_LONG).show()
                    }



                }
            }
        }


    }

    private fun alert(placeName: String, latitude: Double, longitude: Double){

        val alert = AlertDialog.Builder(requireContext())
        alert.setTitle("Save Place")
        alert.setMessage("Are You Sure To The Place $placeName")
        alert.setPositiveButton("Continue",object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {

                val intent = Intent(requireContext(),AddPlaceActivity::class.java)
                if (placeName != ""){
                    intent.putExtra("placeName",placeName)
                }
                intent.putExtra("latitude",latitude)
                intent.putExtra("longitude",longitude)
                startActivity(intent)

            }
        })
        alert.setNegativeButton("Cancel"){ p0, p1 ->

            p0.dismiss()
            mMap.clear()
        }
        alert.setCancelable(true)
        alert.create()
        alert.show()


    }
    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"
        private const val DEFAULT_ZOOM = 17f

        fun newInstance(latitude: Double, longitude: Double): MapsFragment {
            val fragment = MapsFragment()
            val args = Bundle()
            args.putDouble(ARG_LATITUDE, latitude)
            args.putDouble(ARG_LONGITUDE, longitude)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}