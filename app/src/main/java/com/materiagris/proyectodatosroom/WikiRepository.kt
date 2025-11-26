package com.materiagris.proyectodatosroom


// Repositorio que maneja las operaciones de datos.
// Actua como intermediario entre el ViewModel y el origen de los datos (WikiDao/Room).
// Su proposito es abstraer la fuente de datos.

class WikiRepository(private val dao: WikiDao) {

    // Metodos para leer datos, simplemente delegan la llamada al DAO.
    suspend fun getJuegos() = dao.getAllJuegos()
    suspend fun getConsolas() = dao.getAllConsolas()

    // Metodos para agregar datos.
    suspend fun addJuego(j: Juego) = dao.insertJuego(j)
    suspend fun addConsola(c: Consola) = dao.insertConsola(c)

    // Metodos para actualizar y eliminar datos.
    suspend fun updateJuego(j: Juego) = dao.updateJuego(j)
    suspend fun deleteJuego(j: Juego) = dao.deleteJuego(j)

    suspend fun updateConsola(c: Consola) = dao.updateConsola(c)
    suspend fun deleteConsola(c: Consola) = dao.deleteConsola(c)
}