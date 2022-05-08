package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.base.BaseFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var currentReminderLocation: PointOfInterest? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    companion object {
        private const val TAG = "SelectLocationFragment"
        private val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // create geo location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.save.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.apply {
            currentReminderLocation?.let {
                selectedPOI.value = it
                latitude.value = it.latLng.latitude
                longitude.value = it.latLng.longitude
                reminderSelectedLocationStr.value = it.name
            }
        }
        parentFragmentManager.popBackStack()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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

    override fun onMapReady(p0: GoogleMap?) {
        Log.d(TAG, "The map is loaded")
        map = p0!!

        enableMyLocation()
        setPOILocationReminderAfterLongClick(map)
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isForegroundGeoLocationPermissionGranted()) {
            map.isMyLocationEnabled = true

            // set map style. Note, you might get error in log if map is not NORMAL
            setMapStyle(map)

            // Move camera to the current location.
            focusMapToCurrentLocation(map)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun setPOILocationReminderAfterLongClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            // Clear previously set POI for location reminder if any
            map.clear()

            map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            ).showInfoWindow()

            // Update the current POI location reminder
            currentReminderLocation = poi
            binding.save.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun focusMapToCurrentLocation(map: GoogleMap) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val (lat, lng) = it.run { latitude to longitude }
                    map.moveCamera(
                        CameraUpdateFactory
                            .newLatLng(LatLng(lat, lng))
                    )
                    map.animateCamera(CameraUpdateFactory.zoomTo(15f), 1000, null)
                }
                //null we do nothing, not the best. We are assuming, user won't turn off
                //location service after the first activity.
            }
            .addOnFailureListener {
                Log.e(TAG, it.message, it)
            }
    }

    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: NoSuchFileException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
}
