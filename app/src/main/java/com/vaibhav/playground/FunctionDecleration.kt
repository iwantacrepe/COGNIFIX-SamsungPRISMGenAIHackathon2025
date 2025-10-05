package com.vaibhav.playground

import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool

/**
 * Defines all function declarations (tools) Gemini can call.
 */
object FunctionDeclarations {

    // WEATHER TOOLS
    val getCoordinatesTool = FunctionDeclaration(
        name = "getCoordinates",
        description = """
            Get geographic coordinates (latitude & longitude) for a given city name 
            using OpenWeather Geocoding API. 
            Use this when you need precise coordinates before calling weather or place APIs.
            Returns: { lat: Double, lon: Double }
        """.trimIndent(),
        parameters = mapOf(
            "city" to Schema.string("Name of the city or location to geocode.")
        )
    )

    val fetchWeatherTool = FunctionDeclaration(
        name = "fetchWeather",
        description = """
            Fetch current weather and basic forecast data for a given latitude and longitude
            using OpenWeather API.
            Returns: { temperature, humidity, condition, windSpeed, description }
        """.trimIndent(),
        parameters = mapOf(
            "lat" to Schema.double("Latitude of the location."),
            "lon" to Schema.double("Longitude of the location.")
        )
    )

    // FINANCE TOOLS
    val fetchStockDataTool = FunctionDeclaration(
        name = "fetchStockData",
        description = """
            Fetch live stock price, percentage change, market cap, and volume data 
            using Financial Modeling Prep API.
            Use when user asks for stock/company info (e.g., "price of AAPL" or "Netflix stock performance").
            Returns: { symbol, name, price, change, changePercentage, marketCap, exchange, ... }
        """.trimIndent(),
        parameters = mapOf(
            "query" to Schema.string("Ticker symbol or company name (e.g., AAPL, TSLA, Netflix).")
        )
    )

    val fetchExchangeRateTool = FunctionDeclaration(
        name = "fetchExchangeRate",
        description = """
            Fetch real-time currency exchange rate between two supported currencies 
            (USD, INR, EUR, GBP, JPY, etc.) using Financial Modeling Prep API.
            Use when user asks about forex rates or currency conversions.
            Returns: { base, target, rate }
        """.trimIndent(),
        parameters = mapOf(
            "base" to Schema.string("Base currency code, e.g., USD."),
            "target" to Schema.string("Target currency code, e.g., INR.")
        )
    )

    //  TRAVEL & PLACES TOOLS
    val fetchNearbyPlacesTool = FunctionDeclaration(
        name = "fetchNearbyPlaces",
        description = """
            Retrieve nearby points of interest (restaurants, hotels, attractions, etc.)
            based on location coordinates using Google Places API.
            Use when user asks for nearby recommendations or attractions.
            Returns: { name, rating, address, distance }
        """.trimIndent(),
        parameters = mapOf(
            "lat" to Schema.double("Latitude of the location."),
            "lon" to Schema.double("Longitude of the location."),
            "type" to Schema.string("Type of place (e.g., restaurant, hotel, museum, cafe).")
        )
    )

    val fetchFlightsTool = FunctionDeclaration(
        name = "fetchFlights",
        description = """
            Retrieve current flight options between two cities or airports 
            using Google Search results.
            Use when user asks for flight routes, schedules, or ticket availability.
            Returns: summarized flight data (airline, duration, price range, stops)
        """.trimIndent(),
        parameters = mapOf(
            "source" to Schema.string("Departure city or airport."),
            "destination" to Schema.string("Arrival city or airport."),
            "date" to Schema.string("Date of travel in YYYY-MM-DD format (optional).")
        )
    )

    // GENERIC WEB SEARCH TOOL
    val fetchWebSearchResultsTool = FunctionDeclaration(
        name = "fetchWebSearchResults",
        description = """
            Perform a real-time Google Search using the Serper.dev API 
            to retrieve up-to-date and relevant information.
            
            Use ONLY when:
            • The user asks about recent or factual topics not in your training knowledge.
            • The query requires external data (e.g., "latest iPhone reviews", "flights from Delhi to Tokyo").
            
            Do NOT use for subjective, historical, or reasoning questions.
            Returns: top web snippets with title, link, and short description.
        """.trimIndent(),
        parameters = mapOf(
            "query" to Schema.string(
                "The search text (e.g., 'best restaurants in Paris', 'AI research papers 2024', 'news about Tesla')."
            )
        )
    )

    // AGGREGATE TOOL REGISTRATION
    val tools = listOf(
        Tool.functionDeclarations(
            listOf(
                getCoordinatesTool,
                fetchWeatherTool,
                fetchStockDataTool,
                fetchExchangeRateTool,
                fetchNearbyPlacesTool,
                fetchFlightsTool,
                fetchWebSearchResultsTool
            )
        )
    )
}