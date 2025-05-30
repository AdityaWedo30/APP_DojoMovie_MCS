package com.example.pra_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment

        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        // Contoh: Tambah marker dan pindahkan kamera ke lokasi tertentu
        val dojoLocation = LatLng( -6.5604262, 106.7661093) // Jakarta
        myMap.addMarker(MarkerOptions().position(dojoLocation).title("DOJO Location"))
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dojoLocation, 12f))
    }
}
