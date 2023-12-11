package edu.training.droidbountyhunterkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.network.NetworkServices
import edu.training.droidbountyhunterkotlin.network.OnTaskListener
import edu.training.droidbountyhunterkotlin.utils.PictureTools
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime

class DetalleActivity : AppCompatActivity() {
    var fugitivo: Fugitivo? = null
    var database: DatabaseBountyHunter? = null
    private var UDID: String? = ""
    private val REQUEST_CODE_GPS = 1234
    private val REQUEST_CODE_PHOTO_IMAGE = 1888

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private var direccionImagen: Uri? = null
    private var pictureFugitive: ImageView? = null

    private var botonCapturar: Button? = null

    private lateinit var menuCapturar: View
    private lateinit var menuPhoto: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)
        setupLocationObjects()

        val botonMapa = findViewById<Button>(R.id.botonMapa)
        val etiquetaMensaje = findViewById<TextView>(R.id.etiquetaMensaje)
        pictureFugitive = findViewById(R.id.pictureFugitivo)
        botonMapa.setOnClickListener { onMapClick() }
        fugitivo = intent.extras?.get("fugitivo") as Fugitivo
        title = fugitivo!!.name + " - " + fugitivo!!.id

        botonCapturar = findViewById(R.id.botonCapturar)
        if (fugitivo!!.status == 0) {
            etiquetaMensaje.text = "El fugitivo sigue suelto..."
        } else {
            etiquetaMensaje.text = "Atrapado!!!"
            botonCapturar?.visibility = View.GONE
            val bitmap = fugitivo!!.photo?.let {
                PictureTools.decodeSampledBitmapFromUri(it, 200, 200)
            }
            pictureFugitive?.setImageBitmap(bitmap)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detalle, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()


    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (fugitivo!!.status != 0) {
            menu?.getItem(1)?.setVisible(false)
            menu?.getItem(2)?.setVisible(false)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menuDetalleEliminar -> {
                eliminarFugitivoPresionado(View(this))
                true
            }
            R.id.menuDetalleCapturar -> {
                capturarFugitivoPresionado(View(this))
                true
            }
            R.id.menuDetalleFoto -> {
                OnFotoClick(View(this))
                true
            }
            R.id.menuDetalleMapa -> {
                onMapClick()
                true
            }
            android.R.id.home-> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        apagarGPS()
    }

    override fun onDestroy() {
        pictureFugitive?.setImageBitmap(null)
        System.gc()
        super.onDestroy()
    }

    private fun setupLocationObjects() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.lastLocation != null) {
                    val location = locationResult.lastLocation

                    fugitivo!!.latitude = location!!.latitude
                    fugitivo!!.longitude = location!!.longitude
                } else {
                    Log.d("LocationCallback", "Location missing in callback.")
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun capturarFugitivoPresionado(view: View) {
        database = DatabaseBountyHunter(this)
        fugitivo!!.status = 1
        if (fugitivo!!.photo.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Es necesario tomar la foto antes de capturar al fugitivo",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        fugitivo!!.captureDate = LocalDateTime.now().toString()
        database!!.actualizarFugitivo(fugitivo!!)
        lifecycleScope.launch {
            NetworkServices.execute("Atrapar", object : OnTaskListener {
                override fun tareaCompletada(respuesta: String) {
                    val obj = JSONObject(respuesta)
                    val mensaje = obj.optString("mensaje", "")
                    mensajeDeCerrado(mensaje)
                }

                override fun tareaConError(
                    codigo: Int, mensaje: String, error:
                    String
                ) {
                    Toast.makeText(
                        applicationContext,
                        "Ocurrio un problema en la comunicación con el WebService!!!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }, UDID)
        }
        botonCapturar?.visibility = View.GONE
        val botonEliminar = findViewById<Button>(R.id.botonEliminar)
        botonEliminar.visibility = View.GONE
        setResult(1)
    }

    fun eliminarFugitivoPresionado(view: View) {
        database = DatabaseBountyHunter(this)
        database!!.borrarFugitivos(fugitivo!!)
        setResult(0)
        finish()
    }


    fun OnFotoClick(view: View) {
        if (PictureTools.permissionReadMemmory(this)) {
            obtenFotoDeCamara()
        }
    }

    private fun obtenFotoDeCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        direccionImagen = PictureTools.getOutputMediaFileUri(this, PictureTools.MEDIA_TYPE_IMAGE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, direccionImagen)
        resultLauncher.launch(intent)
    }

    fun mensajeDeCerrado(mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.create()
        builder.setTitle("Alerta!!!")
            .setMessage(mensaje)
            .setOnDismissListener {
                setResult(fugitivo!!.status)
                finish()
            }.show()
    }


    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == Activity.RESULT_OK) {
                fugitivo!!.photo = PictureTools.currentPhotoPath
                val bitmap = PictureTools
                    .decodeSampledBitmapFromUri(PictureTools.currentPhotoPath, 200, 200)
                pictureFugitive?.setImageBitmap(bitmap)
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out
        String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PictureTools.REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("RequestPermissions", "Camera - Granted")
                obtenFotoDeCamara()
            } else {
                Log.d("RequestPermissions", "Camera - Not Granted")
            }
        } else if (requestCode == REQUEST_CODE_GPS) {
            activarGPS()
        }
    }

    @SuppressLint("MissingPermission")
    private fun activarGPS() {
        if (isGPSActivated()) {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!, Looper.myLooper()
            )
            Toast.makeText(this, "Activando GPS...", Toast.LENGTH_LONG).show()
            // Getting last location available
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location:
                                                                      Location? ->
                // Got last known location. In some rare situations this can be null.
                Log.d("CursoKotlin", "Last Known Location: $location")
                location?.let {
                    fugitivo!!.latitude = location.latitude
                    fugitivo!!.longitude = location.longitude
                }
            }
        }
    }

    private fun apagarGPS() {
        try {
            Toast.makeText(this, "Desactivando GPS...", Toast.LENGTH_LONG).show()
            val removeTask =
                fusedLocationClient?.removeLocationUpdates(locationCallback!!)
            removeTask?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LocationRequest", "Location Callback removed.")
                } else {
                    Log.d("LocationRequest", "Failed to remove Location Callback.")
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this, "Error desactivando GPS $e",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun isGPSActivated(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) { // Should we show an explanation
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_CODE_GPS
                    )
                    return false
                } else {
                    //No explanation needed, we can request the permissions.
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_CODE_GPS
                    )
                    return false
                }
            } else {
                return true
            }
        } else {
            return true
        }
    }

    fun onMapClick() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("fugitivo", fugitivo)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_PHOTO_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                fugitivo!!.photo = PictureTools.currentPhotoPath
                val bitmap = PictureTools.decodeSampledBitmapFromUri(
                        PictureTools.currentPhotoPath, 200,
                        200
                    )
                pictureFugitive!!.setImageBitmap(bitmap)
            }
            } else if (requestCode == REQUEST_CODE_GPS) {
            activarGPS()
        }
        }


    }
