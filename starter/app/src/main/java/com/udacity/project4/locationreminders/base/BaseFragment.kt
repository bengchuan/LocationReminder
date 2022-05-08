package com.udacity.project4.locationreminders.base

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    companion object {
        private const val TAG = "BaseFragment"
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 10
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 11
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    }

    protected val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    abstract val _viewModel: BaseViewModel


    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showToast.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showSnackBar.observe(this, Observer {
            Snackbar.make(this.view!!, it, Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showSnackBarInt.observe(this, Observer {
            Snackbar.make(this.view!!, getString(it), Snackbar.LENGTH_LONG).show()
        })

        _viewModel.navigationCommand.observe(this, Observer { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        })
    }

    protected fun isForegroundGeoLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED

    }

    protected fun isBackgroundGeoLocationPermissionGranted(): Boolean {
        return if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }
    }

    protected fun isForegroundAndBackgroundGeoLocationPermissionGranted(): Boolean {
        return isForegroundGeoLocationPermissionGranted() &&
                isBackgroundGeoLocationPermissionGranted()
    }
}