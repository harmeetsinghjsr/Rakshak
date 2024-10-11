package com.example.rakshak

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rakshak.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var  binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        val button1 = findViewById<Button>(R.id.button)
        val button = findViewById<Button>(R.id.button2)
        val text = findViewById<TextView>(R.id.textView2)
        val username = findViewById<EditText>(R.id.editTextText)
        val password = findViewById<EditText>(R.id.editTextTextPassword)
        button.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        button1.setOnClickListener {
            var name = username.text.toString()
            var pass = password.text.toString()
            if(name == "harsh" && pass == "reddy") {
                username.text.clear()
                password.text.clear()
                Toast.makeText(this, "SuccesFull", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainHomePage::class.java)
                startActivity(intent)

            }else {
                Toast.makeText(this, "Wrong Credintial", Toast.LENGTH_SHORT).show()
                text.setText("Try Again")
                username.text.clear()
                password.text.clear()
                text.setTextColor(Color.parseColor("#800000"))
            }
        }

    }
}
