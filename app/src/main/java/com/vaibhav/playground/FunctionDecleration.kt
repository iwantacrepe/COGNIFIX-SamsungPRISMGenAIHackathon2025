package com.vaibhav.playground

import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool

/**
 * Defines function declarations so Gemini knows what tools it can use.
 */
object FunctionDeclarations {

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
            // Use Schema.double() for latitude and longitude
            "lat" to Schema.double("Latitude of the location."),
            "lon" to Schema.double("Longitude of the location.")
        )
    )

    // Register both functions as tools
    val tools = listOf(
        Tool.functionDeclarations(listOf(getCoordinatesTool, fetchWeatherTool))
    )
}
