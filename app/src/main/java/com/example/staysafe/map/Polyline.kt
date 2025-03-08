package com.example.staysafe.map

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun fetchRoutePolyline(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double,
    apiKey: String,
    onRouteFetched: (List<LatLng>) -> Unit
) {
    val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes?key=$apiKey")

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

    withContext(Dispatchers.IO) {
        (url.openConnection() as? HttpURLConnection)?.let { connection ->
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("X-Goog-FieldMask", "routes.polyline")

            connection.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val json = JSONObject(response)
                val route = json.getJSONArray("routes").optJSONObject(0) ?: return@withContext
                val encodedPolyline = route.getJSONObject("polyline").getString("encodedPolyline")

                val decodedPolyline = PolyUtil.decode(encodedPolyline) // ✅ Convert to LatLng list

                withContext(Dispatchers.Main) {
                    onRouteFetched(decodedPolyline)
                }
            } else {
                Log.e("RoutesAPI", "Error fetching route: $responseCode")
            }
        }
    }
}
