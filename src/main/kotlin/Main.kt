package me.kaslo.epicfetcher

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

fun main() {
    // Data classes to deal with parsing weirdness
    data class CentroidCoords(
        val lat: Double,
        val lon: Double
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class EpicEntry(
        val image: String,
        val date: String,
        val centroid_coordinates: CentroidCoords
    )

    // Get our API key
    val nasaApiKey = System.getenv("NASA_API_KEY") ?: "DEMO_KEY"
    if (nasaApiKey == "DEMO_KEY") println("Warning: NASA_API_KEY not defined, falling back to rate-limited DEMO_KEY")

    // Make sure our directory exists
    File("images").mkdirs()

    // Set up HTTP stuff
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.nasa.gov/EPIC/api/images?api_key=$nasaApiKey")
        .header("User-Agent", "EPICFetcher/1.0 (Kotlin; +https://kaslo.me)")
        .build()

    // Make the request, exit if we got an error or empty body
    val response = client.newCall(request).execute()
    if (!response.isSuccessful) {
        println("Request failed with code ${response.code}")
        response.close()
        return
    }
    val responseBody = response.body?.string()
    if (responseBody == null) {
        println("Got empty response. Something very strange happened.")
        response.close()
        return
    }
    response.close()
    
    val mapper = jacksonObjectMapper()
    val data: List<EpicEntry> = mapper.readValue(responseBody)
    // Add header to log
    val logFile = File("epic.log")
    logFile.writeText("Filename | Datetime | Latlong\n")

    println("${data.size} images to save")

    // Iterate through JSON
    for (image in data) {
        val filename = image.image
        val date = image.date
        val lat = image.centroid_coordinates.lat
        val lon = image.centroid_coordinates.lon

        // Write each entry to log file
        logFile.appendText("$filename.png | $date | $lat,$lon\n")

        // Skip fetch if image already exists
        if (File("images", "$filename.png").exists()) {
            println("Already saved $filename.png, skipping...")
            continue
        }

        // Print some stats
        println("Fetching $filename.png...")
        println("Centroid coordinates: $lat,$lon")
        println("Taken at: $date")

        // Fetch and save the image
        val imageRequest = Request.Builder()
            .url("https://api.nasa.gov/EPIC/archive/natural/${date.substring(0,4)}/${date.substring(5,7)}/${date.substring(8,10)}/png/$filename.png?api_key=$nasaApiKey")
            .header("User-Agent", "EPICFetcher/1.0 (Kotlin; +https://kaslo.me)")
            .build()

        // Make sure to wrap in use to close everything when we finish
        client.newCall(imageRequest).execute().use { imageResponse ->
            if (!imageResponse.isSuccessful) {
                println("Request to fetch $filename.png failed with code ${imageResponse.code}")
                return
            }
            val body = imageResponse.body
            if (body == null) {
                println("Response body for image $filename.png is null, something strange is happening.")
                return
            }
            // Write out the image
            // It looks complicated but this is just how it works
            body.byteStream().use { input ->
                File("images", "${filename}.png").outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        println("Successfully saved $filename.png")
    }
}
