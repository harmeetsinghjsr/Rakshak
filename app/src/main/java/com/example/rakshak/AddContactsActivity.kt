package com.example.rakshak

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AddContactsActivity : AppCompatActivity() {
    private lateinit var contactNameInput: EditText
    private lateinit var contactNumberInput: EditText
    private lateinit var addContactButton: Button
    private lateinit var contactListView: ListView
    private lateinit var sendSosButton: Button

    private val contacts = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    if (permissionName == Manifest.permission.ACCESS_FINE_LOCATION) {
                        getCurrentLocation()
                    }
                } else {
                    Toast.makeText(this, "Permission denied: $permissionName", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contacts)

        contactNameInput = findViewById(R.id.contactNameInput)
        contactNumberInput = findViewById(R.id.contactNumberInput)
        addContactButton = findViewById(R.id.addContactButton)
        contactListView = findViewById(R.id.contactListView)
        sendSosButton = findViewById(R.id.sendSosButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sharedPreferences = getSharedPreferences("Contacts", MODE_PRIVATE)
        loadContacts()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contacts)
        contactListView.adapter = adapter

        addContactButton.setOnClickListener { addContact() }
        contactListView.setOnItemClickListener { parent, view, position, id -> deleteContact(position) }
        sendSosButton.setOnClickListener { sendSos() }

        requestPermissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private fun addContact() {
        val name = contactNameInput.text.toString().trim()
        val number = contactNumberInput.text.toString().trim()

        if (name.isNotEmpty() && number.isNotEmpty()) {
            contacts.add("$name: $number")
            saveContacts()
            adapter.notifyDataSetChanged()
            contactNameInput.text.clear()
            contactNumberInput.text.clear()
        } else {
            Toast.makeText(this, "Please enter both name and number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteContact(position: Int) {
        contacts.removeAt(position)
        saveContacts()
        adapter.notifyDataSetChanged()
    }

    private fun saveContacts() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("Contacts", contacts.toSet())
        editor.apply()
    }

    private fun loadContacts() {
        val savedContacts = sharedPreferences.getStringSet("Contacts", emptySet())
        savedContacts?.let { contacts.addAll(it) }
    }

    private fun sendSos() {
        if (contacts.isEmpty()) {
            Toast.makeText(this, "No contacts available to send SOS", Toast.LENGTH_SHORT).show()
            return
        }
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val message = "Emergency! I'm in danger. My location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
                sendSmsToContacts(message)
            } ?: run {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendSmsToContacts(message: String) {
        val smsManager = SmsManager.getDefault()
        contacts.forEach { contact ->
            val number = contact.split(": ")[1]
            smsManager.sendTextMessage(number, null, message, null, null)
        }
        Toast.makeText(this, "SOS messages sent!", Toast.LENGTH_SHORT).show()
    }
}
