package com.vaibhav.playground

import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool

/**
 * Defines all function declarations (tools) Gemini can call.
 */
object FunctionDeclarations {

    // 🌍 Weather-related tools
    val getCoordinatesTool = FunctionDeclaration(
        name = "getCoordinates",
        description = "Get latitude and longitude for a given city using OpenWeather Geocoding API.",
        parameters = mapOf(
            "city" to Schema.string("Name of the city or location to search.")
        )
    )

    val fetchWeatherTool = FunctionDeclaration(
        name = "fetchWeather",
        description = "Fetch current weather for given coordinates using OpenWeather API.",
        parameters = mapOf(
            "lat" to Schema.double("Latitude of the location."),
            "lon" to Schema.double("Longitude of the location.")
        )
    )

    // 💹 Finance-related tool
    val fetchStockDataTool = FunctionDeclaration(
        name = "fetchStockData",
        description = "Fetch live stock price & basic metrics using Financial Modeling Prep. Pass ticker (AAPL) or company name (Netflix).",
        parameters = mapOf(
            "query" to Schema.string("Ticker symbol or company name")
        )
    )
    val fetchNearbyPlacesTool = FunctionDeclaration(
        name = "fetchNearbyPlaces",
        description = "Find nearby points of interest like restaurants, hotels, or tourist spots using Google Places API.",
        parameters = mapOf(
            "lat" to Schema.double("Latitude of the location."),
            "lon" to Schema.double("Longitude of the location."),
            "type" to Schema.string("Type of place to search (e.g., restaurant, hotel, museum).")
        )
    )

    val fetchFlightsTool = FunctionDeclaration(
        name = "fetchFlights",
        description = "Find flight options between two cities using Google Search results.",
        parameters = mapOf(
            "source" to Schema.string("Departure city or airport."),
            "destination" to Schema.string("Arrival city or airport."),
            "date" to Schema.string("Date of travel in YYYY-MM-DD format (optional).")
        )
    )


    val fetchExchangeRateTool = FunctionDeclaration(
        name = "fetchExchangeRate",
        description = "Fetch real-time currency exchange rate between two currencies using Financial Modeling Prep API.",
        parameters = mapOf(
            "base" to Schema.string("Base currency code (e.g., USD)."),
            "target" to Schema.string("Target currency code (e.g., INR).")
        )
    )


    val fetchWebSearchResultsTool = FunctionDeclaration(
        name = "fetchWebSearchResults",
        description = "Search the web for up-to-date information about any topic, location, or entity using Google Search (Serper.dev API).",
        parameters = mapOf(
            "query" to Schema.string("Search query text.")
        )
    )






    // 🧩 Add all to Gemini tool list
    val tools = listOf(
        Tool.functionDeclarations(
            listOf(
                getCoordinatesTool,
                fetchWeatherTool,
                fetchStockDataTool,
                fetchNearbyPlacesTool,
                fetchFlightsTool,
                fetchWebSearchResultsTool,
                fetchExchangeRateTool
            )
        )
    )
}
