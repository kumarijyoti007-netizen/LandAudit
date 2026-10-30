package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LandRecordDao {
    @Query("SELECT * FROM land_records ORDER BY dateScanned DESC")
    fun getAllRecords(): Flow<List<LandRecordEntity>>

    @Query("SELECT * FROM land_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): LandRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LandRecordEntity)

    @Update
    suspend fun updateRecord(record: LandRecordEntity)

    @Delete
    suspend fun deleteRecord(record: LandRecordEntity)

    @Query("DELETE FROM land_records WHERE id = :id")
    suspend fun deleteById(id: String)
}
