package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class HttpRequestExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val url = config["url"]?.toString()
                ?: throw IllegalArgumentException("URL is required for HTTP request")

            val method = config["method"]?.toString()?.uppercase() ?: "GET"
            val headers = config["headers"] as? Map<String, String> ?: emptyMap()
            val body = config["body"]?.toString()
            val timeout = config["timeout"]?.toString()?.toIntOrNull() ?: 30000 // 30 seconds
            val followRedirects = config["followRedirects"]?.toString()?.toBoolean() ?: true
            val allowInsecure = config["allowInsecure"]?.toString()?.toBoolean() ?: false

            executeHttpRequest(url, method, headers, body, timeout, followRedirects, allowInsecure)
        }.onFailure { e ->
            Timber.e(e, "HTTP request failed")
        }
    }

    private suspend fun executeHttpRequest(
        urlString: String,
        method: String,
        headers: Map<String, String>,
        body: String?,
        timeout: Int,
        followRedirects: Boolean,
        allowInsecure: Boolean
    ) {
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null

            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection

                // Configure SSL if allowing insecure connections
                if (allowInsecure && connection is HttpsURLConnection) {
                    setupInsecureSSL(connection)
                }

                // Set timeouts
                connection.connectTimeout = timeout
                connection.readTimeout = timeout

                // Set method
                connection.requestMethod = method

                // Set headers
                headers.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }

                // Default content type for POST/PUT/PATCH if not specified
                if (method in listOf("POST", "PUT", "PATCH") && !headers.containsKey("Content-Type")) {
                    if (body?.contains("=") == true) {
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    } else {
                        connection.setRequestProperty("Content-Type", "application/json")
                    }
                }

                // Handle redirects
                connection.instanceFollowRedirects = followRedirects

                // Set body for methods that support it
                if (method in listOf("POST", "PUT", "PATCH", "DELETE") && body != null) {
                    connection.doOutput = true
                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(body)
                        writer.flush()
                    }
                }

                // Execute request
                val responseCode = connection.responseCode
                Timber.d("HTTP $method $urlString - Response: $responseCode")

                // Read response (optional, for logging)
                val responseBody = try {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } catch (e: Exception) {
                    // Try error stream
                    try {
                        BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                            reader.readText()
                        }
                    } catch (e2: Exception) {
                        ""
                    }
                }

                if (responseCode in 200..299) {
                    Timber.i("HTTP request successful: $responseCode")
                    if (responseBody.isNotEmpty()) {
                        Timber.d("Response: ${responseBody.take(200)}...")
                    }
                } else {
                    throw RuntimeException("HTTP request failed with code $responseCode: ${responseBody.take(100)}")
                }

            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun setupInsecureSSL(connection: HttpsURLConnection) {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            connection.sslSocketFactory = sslContext.socketFactory
            connection.hostnameVerifier = javax.net.ssl.HostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            Timber.w(e, "Failed to setup insecure SSL")
        }
    }

    // Utility method to build form-encoded body
    fun buildFormBody(params: Map<String, String>): String {
        return params.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
    }
}