package com.materiagris.proyectodatosroom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


// Clase abstracta que define la base de datos de Room.
// @Database lista todas las entidades (tablas) y define la version de la base de datos.

@Database(
    entities = [Juego::class, Consola::class], // Lista de todas las clases @Entity.
    version = 1, // Version de la base de datos (importante para las migraciones).
    exportSchema = false // Deshabilita la exportacion del esquema (opcional para proyectos pequeños).
)
abstract class WikiDatabase : RoomDatabase() {

    // Metodo abstracto para obtener el DAO. Room proporciona la implementacion.
    abstract fun wikiDao(): WikiDao

    companion object {
        // @Volatile asegura que el acceso a la variable INSTANCE sea atomico y sincronizado entre hilos.
        @Volatile private var INSTANCE: WikiDatabase? = null
        
        // Metodo Singleton para obtener la instancia de la base de datos.
        // Garantiza que solo exista una instancia de la base de datos.
fun getDatabase(context: Context): WikiDatabase {
            // Devuelve la instancia existente si no es nula.
            return INSTANCE ?: synchronized(this) { // Bloque de sincronizacion para asegurar la unicidad del hilo.
                // Crea la instancia de la base de datos.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WikiDatabase::class.java,
                    "wiki_db" // Nombre del archivo de la base de datos.
                ).build()

                INSTANCE = instance
                instance // Devuelve la instancia.
            }
        }
    }
}