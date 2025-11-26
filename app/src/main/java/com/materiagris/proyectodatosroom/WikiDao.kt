package com.materiagris.proyectodatosroom

import androidx.room.*

/**
 * Data Access Object (DAO) para interactuar con las tablas de Juegos y Consolas.
 * Define las operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para ambas entidades.
 */
@Dao
interface WikiDao {

    // Consulta SQL para obtener todos los juegos de la tabla "juegos".
    @Query("SELECT * FROM juegos")
    suspend fun getAllJuegos(): List<Juego> // 'suspend' indica que es un
    // a función asíncrona (Coroutine).

    // Inserta un nuevo juego. Si hay conflicto (ej. ID duplicado), reemplaza la entrada existente.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJuego(juego: Juego)

    // Actualiza un juego existente. Room lo identifica por el @PrimaryKey.
    @Update
    suspend fun updateJuego(juego: Juego)

    // Elimina un juego. Room lo identifica por el @PrimaryKey.
    @Delete
    suspend fun deleteJuego(juego: Juego)

    // Consulta SQL para obtener todas las consolas.
    @Query("SELECT * FROM consolas")
    suspend fun getAllConsolas(): List<Consola>

    // Inserta una nueva consola (reemplaza si hay conflicto).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsola(consola: Consola)

    // Actualiza una consola existente.
    @Update
    suspend fun updateConsola(consola: Consola)

    // Elimina una consola.
    @Delete
    suspend fun deleteConsola(consola: Consola)
}