package org.abubaker.wander

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.abubaker.wander.databinding.ActivityMapsBinding
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding

    private lateinit var map: GoogleMap

    // JSON Map Styles
    private val TAG = MapsActivity::class.java.simpleName

    // For Live-Location
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)


    }

    private fun setMapStyle(map: GoogleMap) {

        try {

            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            // Ref: https://mapstyle.withgoogle.com/
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }

        } catch (e: Resources.NotFoundException) {

            Log.e(TAG, "Can't find style. Error: ", e)

        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        //
        map = googleMap

        val latitude = 31.341836
        val longitude = 74.140467

        // Add a marker in Sydney and move the camera
        // 1. Geo-location
        val geoAddress = LatLng(latitude, longitude)


        /**
         * 1: World
         * 5: Landmass/continent
         * 10: City
         * 15: Streets
         * 20: Buildings
         */
        val zoomLevel = 15f

        // 2. Position
        // 3. Title
        map.addMarker(MarkerOptions().position(geoAddress).title("My Home"))

        //
        // map.moveCamera(CameraUpdateFactory.newLatLng(geoAddress))

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(geoAddress, zoomLevel))

        /**
         * Overlay image android.png
         */

        // Create a float for the width in meters of the desired overlay
        val overlaySize = 100f

        //
        val androidOverlay = GroundOverlayOptions()

            // Use the BitmapDescriptorFactory.fromResource()method to create a BitmapDescriptor object from the above image.
            .image(BitmapDescriptorFactory.fromResource(R.drawable.android))

            // Set the position property for the GroundOverlayOptions object by calling the position() method
            .position(geoAddress, overlaySize)


        // 1. Call addGroundOverlay() on the GoogleMap object
        // 2. Pass in your GroundOverlayOptions object
        map.addGroundOverlay(androidOverlay)


        setMapLongClick(map)

        setPoiClick(map)

        // Custom Styles
        setMapStyle(map)

        // This will enable the location layer.
        enableMyLocation()

    }

    /**
     * Long Click
     */
    private fun setMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener { geoLocation ->

            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                geoLocation.latitude,
                geoLocation.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(geoLocation)

                    // Set the title of the marker to ???Dropped Pin???
                    .title(getString(R.string.dropped_pin))

                    // set the marker???s snippet to the snippet you just created.
                    .snippet(snippet)

                    // long click markers will now be shown shaded blue
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

        }

    }

    /**
     * Point of Interest
     */
    private fun setPoiClick(map: GoogleMap) {

        map.setOnPoiClickListener { poi ->

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            poiMarker?.showInfoWindow()

        }

    }

    // Inflate: map_options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    // Change the map type based on the user's selection.
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


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }


}
