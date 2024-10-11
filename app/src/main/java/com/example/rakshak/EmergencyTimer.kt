package com.example.rakshak

import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import android.os.Message
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.LocationServices

class EmergencyTimer : AppCompatActivity() {
    private lateinit var timer: CountDownTimer
    private var timerDuration: Long = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val smsPermissionCode = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emergency_timer)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val hoursInput: EditText = findViewById(R.id.hoursInput)
        val minutesInput: EditText = findViewById(R.id.minutesInput)
        val secondsInput: EditText = findViewById(R.id.secondsInput)
        val startButton: Button = findViewById(R.id.startButton)
        val stopButton: Button = findViewById(R.id.stopButton)

        startButton.setOnClickListener {
            val hours = hoursInput.text.toString().toIntOrNull() ?: 0
            val minutes = minutesInput.text.toString().toIntOrNull() ?: 0
            val seconds = secondsInput.text.toString().toIntOrNull() ?: 0

            timerDuration = (hours * 3600 + minutes * 60 + seconds) * 1000L // Convert to milliseconds

            startEmergencyTimer()
        }

        stopButton.setOnClickListener {
            stopEmergencyTimer()
        }
    }
    private fun loadContacts(): Set<String> {
        val sharedPreferences = getSharedPreferences("Contacts", MODE_PRIVATE)
        return sharedPreferences.getStringSet("Contacts", emptySet()) ?: emptySet()
    }
    private fun startEmergencyTimer() {
        timer = object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val timeTextView: TextView = findViewById(R.id.timerTextView)
                timeTextView.text = formatTime(millisUntilFinished)

            }

            override fun onFinish() {
                triggerSOS()
            }
        }.start()
    }
    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = (milliseconds / 1000).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun stopEmergencyTimer() {
        timer.cancel()
    }

    private fun triggerSOS() {
        // Send SOS message
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), smsPermissionCode)
        }
    }

    private fun sendSMS(message: String) {
        val contacts = loadContacts()

        contacts.forEach { contact ->
            val phoneNumber = contact.split(": ")[1] // Extract phone number from "Name: Number"
            try {
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                e.printStackTrace() // Handle the exception
            }
        }
    }
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val message = "Emergency! I'm in danger. My location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
                sendSMS(message)
            } ?: run {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == smsPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                // Permission denied, show a message to the user
            }
        }
    }
}