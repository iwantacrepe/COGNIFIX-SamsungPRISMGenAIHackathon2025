package com.vaibhav.playground

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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

        // Step 1: Normalize company names → ticker symbols
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

        //  Step 2: Financial Modeling Prep API endpoint
        val quoteUrl =
            "https://financialmodelingprep.com/stable/quote?symbol=$encoded&apikey=$FMP_API_KEY"

        //  Step 3: Safe HTTP request
        val quoteBody = httpGetOrNull(quoteUrl)
        if (quoteBody.isNullOrBlank()) return errorJson("No response from FMP API")

        val arr = runCatching { Json.parseToJsonElement(quoteBody).jsonArray }.getOrNull()
        val first = arr?.firstOrNull()?.jsonObject
        if (first == null) {
            return errorJson("No data found for symbol: $symbol")
        }

        // Step 4: Extract all fields safely
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

        // Step 5: Handle invalid or missing API data
        if (price == null) {
            return errorJson("Stock data unavailable or API returned null price for $symbol")
        }

        // Step 6: Return well-structured JSON
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

    private const val GOOGLE_PLACES_API_KEY = "YOUR_GOOGLE_PLACES_API_KEY"
    private const val AVIATIONSTACK_API_KEY = "YOUR_AVIATIONSTACK_API_KEY"


    suspend fun fetchNearbyPlaces(lat: Double, lon: Double, type: String): JsonObject {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$lat,$lon&type=${type.lowercase()}&radius=2000&key=$GOOGLE_PLACES_API_KEY"
        val body = httpGetOrNull(url) ?: return errorJson("No response from Google Places API")

        val results = runCatching { Json.parseToJsonElement(body).jsonObject["results"]?.jsonArray }
            .getOrNull() ?: return errorJson("No nearby $type found")

        val top = results.take(3).mapNotNull { place ->
            val obj = place.jsonObject
            val name = obj["name"]?.jsonPrimitive?.content
            val rating = obj["rating"]?.jsonPrimitive?.doubleOrNull
            val address = obj["vicinity"]?.jsonPrimitive?.content
            if (name != null) JsonObject(mapOf(
                "name" to JsonPrimitive(name),
                "rating" to (rating?.let { JsonPrimitive(it) } ?: JsonNull),
                "address" to JsonPrimitive(address ?: "")
            )) else null
        }

        return JsonObject(mapOf("places" to JsonArray(top)))
    }

    suspend fun fetchFlights(source: String, destination: String, date: String? = null): JsonObject {
        val q = buildString {
            append("Flights from $source to $destination")
            if (!date.isNullOrBlank()) append(" on $date")
        }

        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = """{"q":"$q"}""".toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://google.serper.dev/search")
            .post(body)
            .addHeader("X-API-KEY", SERPER_API_KEY)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val json = response.body?.string() ?: return errorJson("No response from search API")

        val root = runCatching { Json.parseToJsonElement(json).jsonObject }.getOrNull()
            ?: return errorJson("Invalid JSON from Serper")

        val results = root["organic"]?.jsonArray ?: return errorJson("No search results found")

        val flights = results.take(5).mapNotNull { item ->
            val obj = item.jsonObject
            val title = obj["title"]?.jsonPrimitive?.contentOrNull
            val link = obj["link"]?.jsonPrimitive?.contentOrNull
            val snippet = obj["snippet"]?.jsonPrimitive?.contentOrNull
            if (title != null && snippet != null) {
                JsonObject(
                    mapOf(
                        "title" to JsonPrimitive(title),
                        "snippet" to JsonPrimitive(snippet),
                        "link" to JsonPrimitive(link ?: "")
                    )
                )
            } else null
        }

        return JsonObject(mapOf(
            "source" to JsonPrimitive(source),
            "destination" to JsonPrimitive(destination),
            "date" to JsonPrimitive(date ?: "unspecified"),
            "flights" to JsonArray(flights)
        ))
    }


    suspend fun fetchExchangeRate(base: String, target: String): JsonObject {
        val pair = (base + target).uppercase()
        val url = "https://financialmodelingprep.com/stable/quote-short?symbol=$pair&apikey=$FMP_API_KEY"

        val body = httpGetOrNull(url) ?: return errorJson("No response from FMP Forex API")

        val arr = runCatching { Json.parseToJsonElement(body).jsonArray }.getOrNull()
        val first = arr?.firstOrNull()?.jsonObject ?: return errorJson("No data found for $pair")

        val rate = first["price"]?.jsonPrimitive?.doubleOrNull ?: return errorJson("Invalid response")
        val change = first["change"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val volume = first["volume"]?.jsonPrimitive?.longOrNull ?: 0L

        return JsonObject(
            mapOf(
                "symbol" to JsonPrimitive(pair),
                "rate" to JsonPrimitive(rate),
                "change" to JsonPrimitive(change),
                "volume" to JsonPrimitive(volume)
            )
        )
    }

    private const val SERPER_API_KEY = "bd86ce7c38b447ce99e72aaa46a4940933360517"

    suspend fun fetchWebSearchResults(query: String): JsonObject {
        val encodedQuery = query.trim()
        if (encodedQuery.isEmpty()) return errorJson("Empty search query")

        try {
            // Build JSON body
            val jsonBody = """{"q":"$encodedQuery"}"""
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            // Build POST request
            val request = Request.Builder()
                .url("https://google.serper.dev/search")
                .post(requestBody)
                .addHeader("X-API-KEY", SERPER_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build()

            // Execute call
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful)
                    return errorJson("HTTP ${response.code} from Serper API")

                val responseBody = response.body?.string() ?: return errorJson("Empty response body")
                val json = Json.parseToJsonElement(responseBody).jsonObject

                // Extract first few organic results
                val results = json["organic"]?.jsonArray?.take(3)?.mapNotNull { item ->
                    val obj = item.jsonObject
                    val title = obj["title"]?.jsonPrimitive?.contentOrNull
                    val snippet = obj["snippet"]?.jsonPrimitive?.contentOrNull
                    val link = obj["link"]?.jsonPrimitive?.contentOrNull
                    if (title != null && snippet != null && link != null) {
                        JsonObject(
                            mapOf(
                                "title" to JsonPrimitive(title),
                                "snippet" to JsonPrimitive(snippet),
                                "link" to JsonPrimitive(link)
                            )
                        )
                    } else null
                } ?: emptyList()

                return JsonObject(mapOf("results" to JsonArray(results)))
            }
        } catch (e: Exception) {
            return errorJson("Search error: ${e.localizedMessage}")
        }
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