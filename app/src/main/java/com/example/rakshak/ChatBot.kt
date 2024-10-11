package com.example.rakshak

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatBot : AppCompatActivity() {
    private val client = OkHttpClient()
    private val apikey = BuildConfig.botkey
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_bot)

        val input = findViewById<EditText>(R.id.input)
        val output = findViewById<TextView>(R.id.output)
        val button = findViewById<Button>(R.id.buttonbot)

        button.setOnClickListener {
            val text = input.text.toString()
            CoroutineScope(Dispatchers.Main).launch {
                val resp = getResponse(text)
                output.text = resp
            }
        }

    }
    private suspend fun getResponse(question:String) : String = withContext(Dispatchers.IO){
        return@withContext try {
            val generativeModel =
                GenerativeModel(
                    // Specify a Gemini model appropriate for your use case
                    modelName = "gemini-1.5-flash",
                    // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                    apiKey = apikey
                )

            val prompt =
                "I will Provide you the context only answer it if's womens safety related or answer in way that you are woman safety related bot but not more than 2 lines , following is this context: " + question
            val response = generativeModel.generateContent(prompt)
            (response.text.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message}"
        }
    }
}

