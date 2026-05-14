package com.example.mybusiness.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Trabajador::class, Cliente::class, Gasto::class, Ingreso::class, HistorialMes::class, Categoria::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trabajadorDao(): TrabajadorDao
    abstract fun clienteDao(): ClienteDao
    abstract fun gastoDao(): GastoDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun historialMesDao(): HistorialMesDao
    abstract fun categoriaDao(): CategoriaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_business_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
