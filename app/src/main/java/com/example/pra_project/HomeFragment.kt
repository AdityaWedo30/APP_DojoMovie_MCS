package com.example.pra_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
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
        recyclerView = view.findViewById(R.id.rv_item_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        requestQueue = Volley.newRequestQueue(requireContext())

        // Fetch data
        val url = "https://api.npoint.io/a3c106cda8c706c8e96f"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null, { response ->
                try {
                    val itemList = parseJSON(response)
                    val adapter = ItemAdapter(itemList)
                    recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, { error ->
                Log.e("Volley error", error.toString())
            }
        )
        requestQueue.add(request)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        val dojoLocation = LatLng(-6.2088, 106.8456)
        myMap.addMarker(MarkerOptions().position(dojoLocation).title("DOJO Location"))
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dojoLocation, 12f))
    }

    private fun parseJSON(jsonObject: JSONObject): ArrayList<Item> {
        val itemList = ArrayList<Item>()
        try {
            val itemArray = jsonObject.getJSONArray("items")
            for (i in 0 until itemArray.length()) {
                val itemObject = itemArray.getJSONObject(i)
                val itemImage = itemObject.getString("image")
                val itemName = itemObject.getString("title")
                val itemPrice = itemObject.getInt("price")
                itemList.add(Item(itemImage, itemName, itemPrice))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return itemList
    }
}