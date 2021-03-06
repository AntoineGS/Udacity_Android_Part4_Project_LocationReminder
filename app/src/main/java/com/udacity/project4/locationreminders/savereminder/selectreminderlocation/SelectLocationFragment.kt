package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private var marker: Marker? = null
    private var isLocationPermissionGranted: Boolean = true

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            isLocationPermissionGranted = isGranted and isLocationPermissionGranted

            if (!isGranted) {
                Toast.makeText(context, R.string.permission_error, Toast.LENGTH_LONG).show()
            }
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
            LocationServices.getFusedLocationProviderClient(requireActivity());

        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        requestLocationPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync(this)
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
        if (p0 != null) {
            map = p0
            customizeMapStyle()
            setPoiClick()
            onLocationSelected()
            showMyCurrentLocation()
        }
    }

    private fun customizeMapStyle() {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Toast.makeText(context, R.string.custom_theme_error, Toast.LENGTH_LONG).show()
            }
        } catch (e: Resources.NotFoundException) {
            Toast.makeText(context, R.string.custom_theme_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun setPoiClick() {
        map.setOnPoiClickListener { poi ->
            _viewModel.selectedPOI.postValue(poi)
            _viewModel.latitude.postValue(poi.latLng.latitude)
            _viewModel.longitude.postValue(poi.latLng.longitude)
            _viewModel.reminderSelectedLocationStr.postValue(poi.name)
            _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        }
    }

    private fun onLocationSelected() {
        map.setOnMapLongClickListener { latLng ->
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )

            _viewModel.reminderSelectedLocationStr.postValue(
                "%.5f, %.5f".format(latLng.latitude, latLng.longitude)
            )
            _viewModel.latitude.postValue(latLng.latitude)
            _viewModel.longitude.postValue(latLng.longitude)
            _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        }
    }

    @SuppressLint("MissingPermission") // IDE doesn't seem to like when permission check is in another method
    private fun showMyCurrentLocation() {
        if (isLocationPermissionGranted) {
            map.isMyLocationEnabled = true
            getCurrentLocation()

            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireContext())

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location.let {
                        getCurrentLocation()

                        binding.saveButton.setOnClickListener {
                            location.let {
                                _viewModel.showToast.postValue(getString(R.string.my_current_location))
                                _viewModel.latitude.postValue(location?.latitude)
                                _viewModel.longitude.postValue(location?.longitude)
                                _viewModel.reminderSelectedLocationStr.postValue(getString(R.string.my_current_location))
                                _viewModel.navigationCommand.postValue(NavigationCommand.Back)
                            }
                        }
                    }
                }
        }
    }

    @SuppressLint("MissingPermission") // Doesn't seem to like my bool approach..
    private fun getCurrentLocation() {
        if (isLocationPermissionGranted) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val homeLatLng = LatLng(it.latitude, it.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15F))
                    }
                }
        }

    }
}
