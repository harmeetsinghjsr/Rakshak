package com.example.rakshak

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment

class GoogleMaps : AppCompatActivity() {
    private lateinit var mMap : GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_google_maps)


//        val mapFragment = supportFragmentManager.findFragmentById(
//            R.id.map
//        ) as? SupportMapFragment
//        mapFragment?.getMapAsync { googleMap ->
//            addMarkers(googleMap)
//        }



    }
}