package com.example.trialchecker

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultText = findViewById(R.id.resultText)

        val accountEmail = getPrimaryGoogleAccount()
        if (accountEmail != null) {
            checkTrialStatus(accountEmail)
        } else {
            resultText.text = "No Google account found!"
        }
    }

    private fun getPrimaryGoogleAccount(): String? {
        val accountManager = AccountManager.get(this)
        val accounts: Array<Account> = accountManager.getAccountsByType("com.google")
        return if (accounts.isNotEmpty()) accounts[0].name else null
    }

    private fun checkTrialStatus(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().put("email", email)
                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8000/check_trial") // Localhost for emulator
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string() ?: "Error"

                runOnUiThread {
                    resultText.text = "Server Response: $responseData"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    resultText.text = "Error: ${e.message}"
                }
            }
        }
    }
}
