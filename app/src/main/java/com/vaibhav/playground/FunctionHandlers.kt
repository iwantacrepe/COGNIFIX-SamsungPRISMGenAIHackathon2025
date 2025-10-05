package com.vaibhav.playground

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.net.URLEncoder

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


    private val client = OkHttpClient()
    private const val FMP_API_KEY = "QGA7OiyDuV2XeGcGBDhQOfThCboJEtuh"

    suspend fun fetchStockData(queryOrSymbol: String): JsonObject {
        val q = queryOrSymbol.trim()
        if (q.isEmpty()) return errorJson("Empty stock query")

        // ✅ Step 1: Normalize company names → ticker symbols
        val symbol = when {
            q.equals("apple", ignoreCase = true) -> "AAPL"
            q.equals("google", ignoreCase = true) -> "GOOGL"
            q.equals("microsoft", ignoreCase = true) -> "MSFT"
            q.equals("amazon", ignoreCase = true) -> "AMZN"
            q.equals("netflix", ignoreCase = true) -> "NFLX"
            q.equals("tesla", ignoreCase = true) -> "TSLA"
            else -> q.uppercase()
        }

        val encoded = URLEncoder.encode(symbol, Charsets.UTF_8.name())

        // ✅ Step 2: Financial Modeling Prep API endpoint
        val quoteUrl =
            "https://financialmodelingprep.com/stable/quote?symbol=$encoded&apikey=$FMP_API_KEY"

        // ✅ Step 3: Safe HTTP request
        val quoteBody = httpGetOrNull(quoteUrl)
        if (quoteBody.isNullOrBlank()) return errorJson("No response from FMP API")

        val arr = runCatching { Json.parseToJsonElement(quoteBody).jsonArray }.getOrNull()
        val first = arr?.firstOrNull()?.jsonObject
        if (first == null) {
            return errorJson("No data found for symbol: $symbol")
        }

        // ✅ Step 4: Extract all fields safely
        val name = first["name"]?.jsonPrimitive?.content ?: symbol
        val price = first["price"]?.jsonPrimitive?.doubleOrNull
        val change = first["change"]?.jsonPrimitive?.doubleOrNull
        val changePct = first["changePercentage"]?.jsonPrimitive?.doubleOrNull
        val marketCap = first["marketCap"]?.jsonPrimitive?.longOrNull
        val exchange = first["exchange"]?.jsonPrimitive?.contentOrNull
        val volume = first["volume"]?.jsonPrimitive?.longOrNull
        val open = first["open"]?.jsonPrimitive?.doubleOrNull
        val prevClose = first["previousClose"]?.jsonPrimitive?.doubleOrNull
        val dayHigh = first["dayHigh"]?.jsonPrimitive?.doubleOrNull
        val dayLow = first["dayLow"]?.jsonPrimitive?.doubleOrNull
        val yearHigh = first["yearHigh"]?.jsonPrimitive?.doubleOrNull
        val yearLow = first["yearLow"]?.jsonPrimitive?.doubleOrNull

        // ✅ Step 5: Handle invalid or missing API data
        if (price == null) {
            return errorJson("Stock data unavailable or API returned null price for $symbol")
        }

        // ✅ Step 6: Return well-structured JSON
        return JsonObject(
            mapOf(
                "symbol" to JsonPrimitive(symbol),
                "name" to JsonPrimitive(name),
                "price" to JsonPrimitive(price),
                "change" to (change?.let { JsonPrimitive(it) } ?: JsonNull),
                "changePercentage" to (changePct?.let { JsonPrimitive(it) } ?: JsonNull),
                "marketCap" to (marketCap?.let { JsonPrimitive(it) } ?: JsonNull),
                "exchange" to (exchange?.let { JsonPrimitive(it) } ?: JsonNull),
                "volume" to (volume?.let { JsonPrimitive(it) } ?: JsonNull),
                "open" to (open?.let { JsonPrimitive(it) } ?: JsonNull),
                "previousClose" to (prevClose?.let { JsonPrimitive(it) } ?: JsonNull),
                "dayHigh" to (dayHigh?.let { JsonPrimitive(it) } ?: JsonNull),
                "dayLow" to (dayLow?.let { JsonPrimitive(it) } ?: JsonNull),
                "yearHigh" to (yearHigh?.let { JsonPrimitive(it) } ?: JsonNull),
                "yearLow" to (yearLow?.let { JsonPrimitive(it) } ?: JsonNull)
            )
        )
    }


    // --- Helpers
    private fun httpGetOrNull(url: String): String? = try {
        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string()
            if (!resp.isSuccessful || body.isNullOrBlank()) null else body
        }
    } catch (_: Exception) {
        null
    }

    private fun errorJson(msg: String) = JsonObject(mapOf("error" to JsonPrimitive(msg)))


}
