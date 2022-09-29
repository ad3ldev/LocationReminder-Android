package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var selectedLocName: String = ""
    private var selectedLocLat: Double = 0.0
    private var selectedLocLng: Double = 0.0
    private var activeMarker: Marker? = null

    private lateinit var cancellationSource: CancellationTokenSource

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(
                    requireContext(),
                    "Foreground location permission is not granted",
                    Toast.LENGTH_LONG
                ).show()
            }
            foregroundLocationPermission(granted)
        }

    override fun onStart() {
        super.onStart()
        cancellationSource = CancellationTokenSource()
    }

    override fun onStop() {
        super.onStop()
        cancellationSource.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment =
            childFragmentManager.findFragmentById(binding.googleMap.id) as SupportMapFragment
        mapFragment.getMapAsync(this) // calling onMapReady()
        binding.confirmButton.setOnClickListener {
            if (selectedLocName.isEmpty()) {
                Toast.makeText(requireContext(), "No location is selected!", Toast.LENGTH_LONG)
                    .show()
            } else {
                onLocationSelected()
                findNavController().popBackStack()
            }
        }
        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.reminderSelectedLocationStr.value = selectedLocName
        _viewModel.latitude.value = selectedLocLat
        _viewModel.longitude.value = selectedLocLng
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

    private fun foregroundLocationPermission(granted: Boolean?) {
        if (granted == true) {
            updateMapUISettings(true)
            getDeviceLocation {
                val zoomLevel = 15f
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, zoomLevel))
                selectedLocName = "Default Current Location"
                selectedLocLat = it.latitude
                selectedLocLng = it.longitude
                onLocationSelected()
                activeMarker?.remove()
                activeMarker = map.addMarker(MarkerOptions().position(it))
            }
        } else {
            updateMapUISettings(false)
        }
        setMapLongClick(map)
        setPOIClick(map)
        setMapStyle(map)
    }

    private fun updateMapUISettings(granted: Boolean) {
        try {
            map.isMyLocationEnabled = granted
            map.uiSettings.isMyLocationButtonEnabled = granted
        } catch (e: SecurityException) {
            Timber.e(e.message)
        }
    }

    private fun getDeviceLocation(callback: (LatLng) -> Unit) {
        try {
            val locationResult = fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationSource.token
            )
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val latLng = LatLng(task.result.latitude, task.result.longitude)
                    callback(latLng)
                }
            }
        } catch (e: SecurityException) {
            Timber.e(e.message)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            selectedLocName = "Custom Location"
            selectedLocLat = latLng.latitude
            selectedLocLng = latLng.longitude
            onLocationSelected()
            activeMarker?.remove()
            activeMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(selectedLocName)
                    .snippet(snippet)
            )
        }
    }

    private fun setPOIClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            activeMarker?.remove()
            activeMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            activeMarker?.showInfoWindow()

            selectedLocName = poi.name
            selectedLocLat = poi.latLng.latitude
            selectedLocLng = poi.latLng.longitude
            onLocationSelected()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Timber.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e(e.message)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val granted = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (granted) {
            foregroundLocationPermission(true)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
