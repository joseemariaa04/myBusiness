package com.example.mybusiness.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Trabajador::class, Cliente::class, Gasto::class, Ingreso::class, HistorialMes::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trabajadorDao(): TrabajadorDao
    abstract fun clienteDao(): ClienteDao
    abstract fun gastoDao(): GastoDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun historialMesDao(): HistorialMesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Si la columna ya existe por un destructive migration previo, esto podría fallar, 
                // pero en una migración limpia es necesario.
                try {
                    db.execSQL("ALTER TABLE ingresos ADD COLUMN categoria TEXT NOT NULL DEFAULT 'Venta'")
                } catch (e: Exception) {
                    // Columna ya existe
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `historial_meses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombreMes` TEXT NOT NULL, `ingresosTotales` REAL NOT NULL, `gastosTotales` REAL NOT NULL, `beneficio` REAL NOT NULL, `fecha` INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_business_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
