package de.ju.coozy.core.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

object ResourceLoader {

    fun loadShaderSource(resourcePath: String): String? {
        try {
            ResourceLoader::class.java.getResourceAsStream(resourcePath).use { `in` ->
                requireNotNull(`in`) { "Resource not found: $resourcePath" }
                BufferedReader(InputStreamReader(`in`, StandardCharsets.UTF_8)).use { reader ->
                    return reader.lines().collect(Collectors.joining("\n"))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Error during loading shader: $resourcePath", e)
        }
    }

}