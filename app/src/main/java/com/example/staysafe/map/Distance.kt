package com.example.staysafe.map

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
suspend fun fetchDistanceAndDuration(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double,
    apiKey: String
): Pair<String, String>? {
    val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes?key=$apiKey")

    Log.d("RoutesAPI", "Calling URL: $url")

    Log.d("RoutesAPI", "Origin: $originLat, $originLng")
    Log.d("RoutesAPI", "Destination: $destLat, $destLng")

    val departureTime = Instant.now().toString()

    val body = """
    {
        "origin": {
            "location": {
                "latLng": {
                    "latitude": $originLat,
                    "longitude": $originLng
                }
            }
        },
        "destination": {
            "location": {
                "latLng": {
                    "latitude": $destLat,
                    "longitude": $destLng
                }
            }
        },
        "travelMode": "DRIVE",
        "routingPreference": "TRAFFIC_AWARE",
        "departureTime": "$departureTime",
        "computeAlternativeRoutes": false,
        "routeModifiers": {
            "avoidTolls": false,
            "avoidHighways": false,
            "avoidFerries": false
        },
        "languageCode": "en-US",
        "units": "METRIC"
    }
    """.trimIndent()

    try{

        return withContext(Dispatchers.IO) {
            (url.openConnection() as? HttpURLConnection)?.let { connection ->
                connection.doOutput = true
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.setRequestProperty(
                    "X-Goog-FieldMask",
                    "routes.distanceMeters,routes.duration,routes.legs,routes.polyline,routes.routeLabels"
                )

                connection.outputStream.use { os ->
                    os.write(body.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                Log.d("RoutesAPI", "Response Code: $responseCode")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("RoutesAPI", "Raw Response: $response")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val json = JSONObject(response)
                    if (!json.has("routes") || json.getJSONArray("routes").length() == 0) {
                        Log.e("RoutesAPI", "No routes found!")
                        return@withContext null
                    }

                    val route = json.getJSONArray("routes").getJSONObject(0)
                    val distanceMeters = route.getDouble("distanceMeters")
                    Log.d("RoutesAPI", "Duration String: ${route.getString("duration")}")
                    val durationSeconds = parseDuration(route.getString("duration"))
                    val formattedDuration = formatDuration(durationSeconds)
                    Log.d("RoutesAPI", "Distance: $distanceMeters meters")
                    Log.d("RoutesAPI", "Duration: $durationSeconds seconds")

                    val distanceKm = distanceMeters / 1000

                    return@withContext String.format("%.2f km", distanceKm) to formattedDuration
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error message from server"
                    Log.e("RoutesAPI", "Error Response Code: $responseCode")
                    Log.e("RoutesAPI", "Error Response: $errorResponse")
                    return@withContext null
                }
            }
        }
    } catch (e: Exception) {
        Log.e("RoutesAPI", "Error: ${e.message}")
        return null
    }
}

fun parseDuration(duration: String): Int {
    val regex = Regex("(\\d+(?:\\.\\d+)?)s") // ✅ Extracts float or int before "s"
    val match = regex.find(duration)
    return match?.groupValues?.get(1)?.toDouble()?.toInt() ?: 0
}

fun formatDuration(seconds: Int): String {
    return when {
        seconds < 60 -> "$seconds sec"
        seconds < 3600 -> "${seconds / 60} min"
        else -> {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            if (minutes == 0) "${hours}h"
            else "${hours}h ${minutes}m"
        }
    }
}