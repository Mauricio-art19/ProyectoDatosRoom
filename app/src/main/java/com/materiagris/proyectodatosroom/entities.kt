package com.materiagris.proyectodatosroom

import androidx.room.Entity
import androidx.room.PrimaryKey


//Entidad de Room que representa un videojuego.
//La anotación @Entity mapea esta clase a una tabla en la base de datos SQL.
//tableName = "juegos" especifica el nombre de la tabla.

@Entity(tableName = "juegos")
data class Juego(
    // Clave primaria, se genera automáticamente para cada nueva entrada.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val genero: String,
    val anio: String,
    val desarrollador: String,
    // La URI de la imagen se almacena como String (puede ser nula si no hay imagen).
    val imagenUri: String?
)

// Entidad de Room que representa una consola de videojuegos.
// Mapeada a la tabla "consolas".
@Entity(tableName = "consolas")
data class Consola(
    // Clave primaria, se genera automáticamente.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val fabricante: String,
    val anioLanzamiento: String,
    val generacion: String,
    val imagenUri: String?
)