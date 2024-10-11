package com.example.rakshak

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainHomePage : AppCompatActivity() {

    private val requestPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startVoiceRecognitionService()
        } else {
            Toast.makeText(this, "Permission is required for this functionality.", Toast.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_home_page)
        var helpbtn = findViewById<CardView>(R.id.helpCard)
        var emergencytimerbtn = findViewById<CardView>(R.id.emergencyTimerCard)
        var tempbtn = findViewById<CardView>(R.id.tempCard)
        helpbtn.setOnClickListener {
            val intent = Intent(this, ChatBot::class.java)
            startActivity(intent)
        }

        val btnStartSpeech: Button = findViewById(R.id.btnStartSpeech)

        btnStartSpeech.setOnClickListener {
            checkPermissionsAndStartService()
        }
        var addContactsButton = findViewById<CardView>(R.id.addContactCard)
        addContactsButton.setOnClickListener {
            val intent = Intent(this, AddContactsActivity::class.java)
            startActivity(intent)
        }

        emergencytimerbtn.setOnClickListener {
            val intent = Intent(this, EmergencyTimer::class.java)
            startActivity(intent)
        }

        tempbtn.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)

        }
    }
    private fun checkPermissionsAndStartService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceRecognitionService()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceRecognitionService() {
        val serviceIntent = Intent(this, VoiceRecognitionService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}