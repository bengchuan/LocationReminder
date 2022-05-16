package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.base.BaseFragment
import com.udacity.project4.locationreminders.base.NavigationCommand
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.getLatLng
import com.udacity.project4.utils.isValid
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    companion object {
        private const val TAG = "RemindersActivity"
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 10
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 11
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 12
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        const val ACTION_GEOFENCE_EVENT = "GEOFENCE_EVENT"
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value
            addGeoFence()
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsIsOn(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions:
        Array<out String>, grantResults: IntArray
    ) {

        //Permission Granted
        if (grantResults.getOrNull(LOCATION_PERMISSION_INDEX) == PackageManager.PERMISSION_GRANTED &&
            grantResults.getOrNull(BACKGROUND_LOCATION_PERMISSION_INDEX) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "FG and BG location has been granted!")
            checkDeviceLocationSettingsIsOn()
        } else {
            Snackbar.make(
                requireView(),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                try {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (e: ActivityNotFoundException) {
                    // try to avoid ActivityNotFoundException.
                    Log.e(
                        TAG,
                        "Unable to start activity for : ${Settings.ACTION_APPLICATION_SETTINGS}"
                    )
                    startActivity(Intent().apply {
                        action = Settings.ACTION_SETTINGS
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            }.show()
        }
    }

    private fun checkPermissionsAndStartGeofencing() {
        if (isForegroundAndBackgroundGeoLocationPermissionGranted()) {
            Log.i(TAG, "Permission Granted. Check device location is ON or not")
            checkDeviceLocationSettingsIsOn()
        } else {
            Log.i(TAG, "GeoLocation permission is missing. Requesting it")
            requestForegroundAndBackgroundLocationPermissions()
        }
    }


    private fun checkDeviceLocationSettingsIsOn(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        //if failure
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsIsOn()
                }.show()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun addGeoFence() {
        // Checking GeoLocation Permission
        checkPermissionsAndStartGeofencing()

        // Get reminder
        val reminder = _viewModel.getReminder()
        if (!reminder.isValid()) {
            Log.w(TAG, "Reminder has null lat long. Cannot add geofence")
            return
        }

        val request = createGeoFenceRequest(reminder)
        // add the request
        geofencingClient.addGeofences(request, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Added geofence with location name: ${reminder.location}")
                _viewModel.validateAndSaveReminder(reminder)
                // Navigate back to ReminderList
                _viewModel.navigationCommand.value = NavigationCommand.Back
            }
            addOnFailureListener {
                Snackbar.make(
                    requireView(), R.string.geofences_not_added,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createGeoFence(reminder: ReminderDataItem): Geofence {
        val (lat, lng) = reminder.getLatLng()
        return Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                lat, lng,
                20f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }

    private fun createGeoFenceRequest(reminder: ReminderDataItem): GeofencingRequest {
        val geofence = createGeoFence(reminder)
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }


    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (isForegroundAndBackgroundGeoLocationPermissionGranted())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        requestPermissions(permissionsArray, resultCode)
    }
}
