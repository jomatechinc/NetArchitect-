package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_projects")
data class SavedProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val deviceCount: Int,
    val ciscoConfig: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // INFO, WARNING, ALERT, SUCCESS
    val category: String, // SCANNER, SEC_POLICY, BACKEND, THREAT
    val message: String
)

@Dao
interface NetworkDao {
    @Query("SELECT * FROM saved_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<SavedProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: SavedProject)

    @Query("DELETE FROM saved_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog)

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}

@Database(entities = [SavedProject::class, AuditLog::class], version = 1, exportSchema = false)
abstract class NetworkDatabase : RoomDatabase() {
    abstract fun networkDao(): NetworkDao

    companion object {
        @Volatile
        private var INSTANCE: NetworkDatabase? = null

        fun getDatabase(context: Context): NetworkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetworkDatabase::class.java,
                    "net_architect_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class NetworkRepository(private val networkDao: NetworkDao) {
    val allProjects: Flow<List<SavedProject>> = networkDao.getAllProjects()
    val recentLogs: Flow<List<AuditLog>> = networkDao.getRecentLogs()

    suspend fun saveProject(project: SavedProject) = networkDao.insertProject(project)
    suspend fun deleteProject(id: Int) = networkDao.deleteProjectById(id)
    suspend fun insertLog(log: AuditLog) = networkDao.insertLog(log)
    suspend fun clearLogs() = networkDao.clearLogs()
}
