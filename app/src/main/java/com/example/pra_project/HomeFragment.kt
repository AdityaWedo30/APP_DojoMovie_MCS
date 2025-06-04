package com.example.pra_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.example.dojomovie.DatabaseHelper
import com.example.dojomovie.Film
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException
import org.json.JSONObject

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap
    private lateinit var requestQueue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private var filmList = listOf<Film>()
    private lateinit var filmAdapter: FilmAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init Map
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Init RecyclerView
        dbHelper = DatabaseHelper(requireContext())
        recyclerView = view.findViewById(R.id.rv_item_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        filmList = dbHelper.getAllFilms()
        filmAdapter = FilmAdapter(filmList)
        recyclerView.adapter = filmAdapter


    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        val dojoLocation = LatLng(-6.2088, 106.8456)
        myMap.addMarker(MarkerOptions().position(dojoLocation).title("DOJO Location"))
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dojoLocation, 12f))
    }

}