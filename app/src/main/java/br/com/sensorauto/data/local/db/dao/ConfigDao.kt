package br.com.sensorauto.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.sensorauto.data.local.db.entity.ConfigEntity

@Dao
interface ConfigDao {
    @Query("SELECT * FROM config WHERE id = 1")
    suspend fun get(): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(config: ConfigEntity)
}
