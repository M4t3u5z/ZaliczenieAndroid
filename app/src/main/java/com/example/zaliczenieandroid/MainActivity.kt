package com.example.zaliczenieandroid

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private val alertReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.app.SHOW_ALERT") {
                val message = intent.getStringExtra("MESSAGE")
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView
    private lateinit var messageEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja SharedPreferences
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        // Zarejestruj BroadcastReceiver
        val filter = IntentFilter("com.example.app.SHOW_ALERT")
        registerReceiver(alertReceiver, filter)

        // Pola tekstowe, przyciski i TextView w layoucie
        messageEditText = findViewById(R.id.messageEditText)
        val showButton: Button = findViewById(R.id.showButton)
        val showLocationButton: Button = findViewById(R.id.showLocationButton)
        locationTextView = findViewById(R.id.locationTextView)  // Inicjalizuj TextView

        // Inicjalizacja FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Pobierz wpisany broadcast z SharedPreferences
        val storedMessage = sharedPreferences.getString("STORED_MESSAGE", "")
        messageEditText.setText(storedMessage)

        // Ustaw obsługę kliknięcia na przycisku "Show"
        showButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (message.isNotEmpty()) {
                // Zapisz broadcast do SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("STORED_MESSAGE", message)
                    apply()
                }

                val serviceIntent = Intent(this, BackgroundService::class.java)
                serviceIntent.putExtra("MESSAGE", message)
                startService(serviceIntent)
            } else {
                Toast.makeText(
                    this,
                    "Brak wpisanego tekstu do wyświetlenia",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Ustaw obsługę kliknięcia na przycisku "Show Location"
        showLocationButton.setOnClickListener {
            requestLocation()
        }
    }
        //Uprawnienia do lokalizacji
    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Mamy już uprawnienia, możemy uzyskać lokalizację
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val message = "Aktualna lokalizacja: $latitude, $longitude"
                    Log.d("Location", message)
                    locationTextView.text = message
                    val serviceIntent = Intent(this@MainActivity, BackgroundService::class.java)
                    serviceIntent.putExtra("MESSAGE", message)
                    startService(serviceIntent)
                } ?: run {
                    Log.e("Location", "Nie udało się uzyskać aktualnej lokalizacji")
                    Toast.makeText(
                        this,
                        "Nie udało się uzyskać aktualnej lokalizacji",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } else {
            // Brak uprawnień, poproś użytkownika o nie
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Wyrejestruj BroadcastReceiver
        unregisterReceiver(alertReceiver)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}
