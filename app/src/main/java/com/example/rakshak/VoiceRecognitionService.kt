package com.example.rakshak

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

private const val CHANNEL_ID = "SOS_FOREGROUND_SERVICE_CHANNEL"

class VoiceRecognitionService : Service() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var sirenMediaPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator
    private lateinit var cameraManager: CameraManager
    private var isFlashlightOn = false
    private val phoneNumber = "8084711625" // Replace with your emergency contact number

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {}
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.get(0) ?: ""

            if (recognizedText.contains("HELP", ignoreCase = true)) {
                triggerSOS()
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(recognitionListener)

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation: Location? = locationResult.lastLocation
                if (lastLocation != null) {
                    sendSOSMessages(lastLocation)
                    fusedLocationClient.removeLocationUpdates(this)
                } else {
                    Toast.makeText(this@VoiceRecognitionService, "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Initialize Vibrator and CameraManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Start Speech Recognition
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening for HELP")
        speechRecognizer.startListening(intent)

        // Create and start foreground service notification
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SOS Foreground Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for SOS foreground service"
            }
            notificationManager.createNotificationChannel(channel)
        }
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("SOS Service Running")
            .setContentText("Listening for SOS trigger.") // Replace with your icon
            .build()
    }

    private fun triggerSOS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        soundSiren()
        vibratePhone()
        flashFlashlight()
        sendSOSNotification()
    }

    private fun sendSOSMessages(location: Location) {
        val message = "Emergency SOS triggered from my app.\nLocation: Lat: ${location.latitude}, Lon: ${location.longitude}"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SOS message sent!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSOSNotification() {
        val intent = Intent(this, HomePage::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SOS Alert")
            .setContentText("An SOS has been triggered.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SOS Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for SOS notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun soundSiren() {
        sirenMediaPlayer = MediaPlayer.create(this, R.raw.siren_sound) // Replace with your siren sound resource
        sirenMediaPlayer?.start()
    }

    private fun vibratePhone() {
        vibrator.vibrate(longArrayOf(1000, 1000, 1000), -1)
    }

    private fun flashFlashlight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (isFlashlightOn) {
                    cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
                    isFlashlightOn = false
                } else {
                    cameraManager.setTorchMode(cameraManager.cameraIdList[0], true)
                    isFlashlightOn = true
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error toggling flashlight", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
