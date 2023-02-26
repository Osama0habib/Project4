package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    var currentMarker : Marker? = null
    var pioMarker : Marker? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

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
        if(currentMarker == null && pioMarker == null){
            Toast.makeText(context,"You should select a place",Toast.LENGTH_SHORT).show()
        }else{
            currentMarker.let {
                _viewModel.latitude.value = it?.position?.latitude
                _viewModel.longitude.value = it?.position?.longitude
            }
            pioMarker.let {
                _viewModel.selectedPOI.value = pioMarker?.position?.let { it1 -> PointOfInterest(it1,pioMarker?.title.toString(),pioMarker?.snippet.toString()) }
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

    private fun isPermissionGranted() : Boolean {
        println("isPermissionGranted")
//        return ContextCompat.checkSelfPermission(this.requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return !(ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation(){
        println("enableMyLocation")
        if(isPermissionGranted()){
            map.isMyLocationEnabled = true
            myLocationMarker(map)
        }else{
            println("request Permission")
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
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
          currentMarker =  map.addMarker(
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
        enableMyLocation()
        myLocationMarker(map)

    }
    @SuppressLint("MissingPermission")
    private fun myLocationMarker(map: GoogleMap){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                val mylatLng = location?.let { LatLng(it.latitude, location.longitude) }
                mylatLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
                    ?.let { map.moveCamera(it) }
                mylatLng?.let { MarkerOptions().position(it).title("Me") }
                    ?.let { map.addMarker(it) }
            }
        fusedLocationClient.lastLocation.addOnFailureListener {
            myLocationMarker(map)
        }
    }


}
