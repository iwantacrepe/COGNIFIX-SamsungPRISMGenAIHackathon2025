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

    // 🧩 Add all to Gemini tool list
    val tools = listOf(
        Tool.functionDeclarations(
            listOf(
                getCoordinatesTool,
                fetchWeatherTool,
                fetchStockDataTool
            )
        )
    )
}
