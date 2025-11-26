package com.materiagris.proyectodatosroom

import android.app.Application
import androidx.compose.runtime.mutableStateListOf // Importante para que Compose reaccione a los cambios de la lista.
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch // Para ejecutar operaciones de base de datos en un hilo seguro.

// ViewModel que proporciona los datos a la UI y maneja la logica del negocio.
// Extiende AndroidViewModel para tener acceso al contexto de la aplicacion.

class WikiViewModel(application: Application) : AndroidViewModel(application) {

    // Inicializacion del DAO y el Repositorio usando el patron Singleton de la base de datos.
    private val dao = WikiDatabase.getDatabase(application).wikiDao()
    private val repository = WikiRepository(dao)

    // Listas mutables que son observadas por Compose (Reactivity).
    val juegos = mutableStateListOf<Juego>()
    val consolas = mutableStateListOf<Consola>()

   
    // Bloque de inicializacion que se ejecuta al crear el ViewModel.
    // Carga los datos de la base de datos al inicio.
   
    init {
        viewModelScope.launch { // Usa viewModelScope para lanzar una coroutine que vive mientras el ViewModel este activo.
            juegos.clear()
            consolas.clear()
            // Obtiene todos los datos del repositorio y los añade a las listas reactivas.
            juegos.addAll(repository.getJuegos())
            consolas.addAll(repository.getConsolas())
        }
    }

    // --- Metodos de Agregar ---

    fun addJuego(juego: Juego) {
        viewModelScope.launch {
            repository.addJuego(juego) // Inserta en Room.
            // Recarga completa: Necesario si la entidad obtiene su ID auto-generado solo despues de la insercion.
            juegos.clear()
            juegos.addAll(repository.getJuegos())
        }
    }

    fun addConsola(consola: Consola) {
        viewModelScope.launch {
            repository.addConsola(consola)
            // Recarga completa.
            consolas.clear()
            consolas.addAll(repository.getConsolas())
        }
    }

    // --- Metodos de Actualizar ---

    fun updateJuego(j: Juego) {
        viewModelScope.launch {
            repository.updateJuego(j) // Actualiza en Room.
            // Actualiza la lista reactiva en la posicion correcta (evita una recarga completa).
            val index = juegos.indexOfFirst { it.id == j.id }
            if (index != -1) juegos[index] = j
        }
    }

    fun updateConsola(c: Consola) {
        viewModelScope.launch {
            repository.updateConsola(c)
            // Actualiza la lista reactiva en la posicion correcta.
            val index = consolas.indexOfFirst { it.id == c.id }
            if (index != -1) consolas[index] = c
        }
    }

    // --- Metodos de Eliminar ---

    fun deleteJuego(j: Juego) {
        viewModelScope.launch {
            repository.deleteJuego(j) // Elimina de Room.
            // Elimina el elemento de la lista reactiva.
            juegos.removeAll { it.id == j.id }
        }
    }

    fun deleteConsola(c: Consola) {
        viewModelScope.launch {
            repository.deleteConsola(c)
            // Elimina el elemento de la lista reactiva.
            consolas.removeAll { it.id == c.id }
        }
    }
}