package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
//import kotlinx.android.synthetic.main.activity_reminders.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    var currentMarker: Marker? = null
    var pioMarker: Marker? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        binding.saveButton.setOnClickListener {
            onLocationSelected()


        }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        if (currentMarker == null && pioMarker == null) {
            Toast.makeText(context, R.string.select_location, Toast.LENGTH_SHORT).show()
        } else {
            currentMarker.let {
                _viewModel.latitude.value = it?.position?.latitude
                _viewModel.longitude.value = it?.position?.longitude
            }
            pioMarker.let {
                _viewModel.selectedPOI.value = pioMarker?.position?.let { it1 ->
                    PointOfInterest(
                        it1,
                        pioMarker?.title.toString(),
                        pioMarker?.snippet.toString()
                    )
                }
            }
            view?.findNavController()?.navigateUp()


        }
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID

            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE

            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN

            true
        }
        else -> super.onOptionsItemSelected(item)
    }


//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        println("OnRequestPermissionResult")
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                enableMyLocation()
//            }
//        }
//    }

//    private fun isPermissionGranted(): Boolean {
//        println("isPermissionGranted")
////        return ContextCompat.checkSelfPermission(this.requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        return !(ContextCompat.checkSelfPermission(
//            this.requireContext(),
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//            this.requireContext(),
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED)
//    }

    private fun enableMyLocation() {
        println("enable my location ")
            if (ContextCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_LOCATION_PERMISSION
                )


            }else{
                getLastLocation {
                        location ->
                    myLocationMarker(map,location)

                }

            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        println("on request permission result")
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }else{
                Toast.makeText(context, "location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener { latLng ->
            map.clear()
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            _viewModel.latitude.value = latLng.latitude
            _viewModel.longitude.value = latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = "${latLng.latitude} ${latLng.longitude}"
            currentMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            pioMarker = map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            pioMarker?.showInfoWindow()
            _viewModel.selectedPOI.value = poi
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()
//        myLocationMarker(map)



    }

    @SuppressLint("MissingPermission")
    private fun myLocationMarker(map: GoogleMap,location : Location) {
        println("my location Marker")
        map.isMyLocationEnabled = true
        val mylatLng = LatLng(location.latitude, location.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mylatLng,15f))

//        if (ActivityCompat.checkSelfPermission(
//                this.requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this.requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            ActivityCompat.requestPermissions(
//                this.requireActivity(),
//                arrayOf<String>(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ),
//                REQUEST_LOCATION_PERMISSION
//            )
//
//
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                map.isMyLocationEnabled = true
//                val mylatLng = location?.let { LatLng(it.latitude, location.longitude) }
//                mylatLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
//                    ?.let { map.moveCamera(it) }
//                mylatLng?.let { MarkerOptions().position(it).title("Me") }
//                    ?.let { map.addMarker(it) }
//            }
//        fusedLocationClient.lastLocation.addOnFailureListener {
//            myLocationMarker(map)
//        }
    }

    private fun setMapStyle(map: GoogleMap) {

        try {
            val success =
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.requireContext(), R.raw.map_style))

            if(!success){
                Log.e(ContentValues.TAG,"Style Parsing Failed")
            }
        }
        catch (e :Resources.NotFoundException) {
            Log.e(ContentValues.TAG,"Can't find style. Error : ",e)

        }
    }


    @SuppressLint("MissingPermission")
    fun getLastLocation(locationListener: (Location) -> Unit){
        println("get last location")
        fusedLocationClient.lastLocation.addOnCompleteListener {
            println(it.result)
            val location = it.result
            if(location == null){
                requestNewLocation(locationListener)
            }else{
                locationListener.invoke(location)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun requestNewLocation(locationListener: (Location) -> Unit){
        println("requestNewLocation")
        val locationCallback : LocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                println("onlocationResult : ${p0.lastLocation}")
                locationListener.invoke(p0.lastLocation)
            }

        }

        with(LocationRequest()){
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
            Looper.myLooper()
                ?.let { fusedLocationClient.requestLocationUpdates(this,locationCallback, it) }
        }

    }

}
