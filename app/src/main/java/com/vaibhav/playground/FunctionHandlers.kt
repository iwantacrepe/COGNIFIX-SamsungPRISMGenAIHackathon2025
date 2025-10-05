package com.vaibhav.playground

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL

/**
 * Contains actual API-calling functions for Gemini function-calling.
 */
object FunctionHandlers {

    private const val OPENWEATHER_API_KEY = "9ee374e7aad2002c04653b66d0fcc2c3"  // replace with your key

    /**
     * Get coordinates from a city name
     *    Example: "Kanpur" → { "lat": 26.46, "lon": 80.32 }
     */
    suspend fun getCoordinates(city: String): JsonObject = withContext(Dispatchers.IO) {
        val url =
            "https://api.openweathermap.org/geo/1.0/direct?q=${city}&limit=1&appid=$OPENWEATHER_API_KEY"
        val response = URL(url).readText()
        val json = Json.parseToJsonElement(response).jsonArray
        val obj = json.firstOrNull()?.jsonObject ?: return@withContext JsonObject(mapOf())

        JsonObject(
            mapOf(
                "lat" to obj["lat"]!!,
                "lon" to obj["lon"]!!,
                "name" to obj["name"]!!
            )
        )
    }

    /**
     * ⃣Fetch current weather using lat/lon from OpenWeather One Call API.
     */
    suspend fun fetchWeather(lat: Double, lon: Double): JsonObject = withContext(Dispatchers.IO) {
        val url =
            "https://api.openweathermap.org/data/3.0/onecall?lat=${lat}&lon=${lon}&exclude=minutely,hourly,alerts&units=metric&appid=$OPENWEATHER_API_KEY"
        val response = URL(url).readText()
        val json = Json.parseToJsonElement(response).jsonObject
        val current = json["current"]?.jsonObject ?: return@withContext JsonObject(mapOf())

        val weather = current["weather"]?.jsonArray?.firstOrNull()?.jsonObject
        val desc = weather?.get("description")?.jsonPrimitive?.content ?: "unknown"

        JsonObject(
            mapOf(
                "temperature" to current["temp"]!!,
                "feels_like" to current["feels_like"]!!,
                "humidity" to current["humidity"]!!,
                "description" to JsonPrimitive(desc)
            )
        )
    }
}
