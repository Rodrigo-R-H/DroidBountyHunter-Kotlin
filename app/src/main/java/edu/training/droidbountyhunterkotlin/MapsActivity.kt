package edu.training.droidbountyhunterkotlin

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import edu.training.droidbountyhunterkotlin.models.Fugitivo

class MapsActivity : FragmentActivity(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private var fugitivo: Fugitivo? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        fugitivo = intent.getParcelableExtra("fugitivo")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        title = fugitivo!!.name
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        val position: LatLng
        if (fugitivo!!.latitude == 0.0 && fugitivo!!.longitude == 0.0) {
            position = LatLng(-34.0, 151.0)
        } else {
            position = LatLng(fugitivo!!.latitude, fugitivo!!.longitude)
        }
        googleMap!!.addMarker(
            MarkerOptions().position(position).title(fugitivo!!.name)
        )
        googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 9f))
    }


}