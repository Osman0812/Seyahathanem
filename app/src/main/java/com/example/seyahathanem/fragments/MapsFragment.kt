package com.example.seyahathanem.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FragmentMapsBinding
import com.example.seyahathanem.activities.AddPlaceActivity


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapsFragment : Fragment() , OnMapReadyCallback, OnMapLongClickListener {


    private lateinit var auth: FirebaseAuth

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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        Places.initialize(requireContext(),"AIzaSyCofjcj7zf41IWDl6DoeL9UzV-BYyR0Gd8")

        permissionLauncher()

        sharedPreferences = activity?.getSharedPreferences("com.example.seyahathanem.fragments",
            MODE_PRIVATE)!!

        trackBoolean = false

        selectedLatitude = 0.0
        selectedLongitude = 0.0


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
                mMap.addMarker(MarkerOptions().position(mLocationLatLng))
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
        mMap.clear()
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