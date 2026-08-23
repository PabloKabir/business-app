package com.example.businessapp.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Business>>

    @Query("SELECT * FROM businesses WHERE name LIKE '%' || :q || '%' OR category LIKE '%' || :q || '%' OR phone LIKE '%' || :q || '%' OR address LIKE '%' || :q || '%' OR notes LIKE '%' || :q || '%' ORDER BY name COLLATE NOCASE ASC")
    fun search(q: String): Flow<List<Business>>

    @Insert
    suspend fun insert(item: Business)

    @Update
    suspend fun update(item: Business)

    @Delete
    suspend fun delete(item: Business)
}
