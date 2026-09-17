package de.ju.coozy.core.render.primitives

object Plane {
    val VERTICES: FloatArray = floatArrayOf(
        // Pos (x, y, z),        Normale (nx, ny, nz), UV (u, v)
        -0.5f, 0.0f, -0.5f,      0.0f, 1.0f, 0.0f,     0.0f, 0.0f, // 0: Hinten Links
         0.5f, 0.0f, -0.5f,      0.0f, 1.0f, 0.0f,     1.0f, 0.0f, // 1: Hinten Rechts
         0.5f, 0.0f,  0.5f,      0.0f, 1.0f, 0.0f,     1.0f, 1.0f, // 2: Vorne Rechts
        -0.5f, 0.0f,  0.5f,      0.0f, 1.0f, 0.0f,     0.0f, 1.0f  // 3: Vorne Links
    )

    val INDICES: IntArray = intArrayOf(
        0, 3, 2,
        2, 1, 0
    )
}