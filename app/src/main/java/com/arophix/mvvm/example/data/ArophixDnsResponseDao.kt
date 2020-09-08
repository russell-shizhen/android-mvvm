package com.arophix.mvvm.example.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * The Data Access Object for the [ArophixDnsResponse] class.
 */
@Dao
interface ArophixDnsResponseDao {

    @Query("SELECT * FROM arophix_button_texts ORDER BY id DESC")
    fun getButtonTexts(): LiveData<List<ButtonText>>
    @Query("SELECT * FROM arophix_button_texts WHERE id = :responseId")
    fun getLastButtonText(responseId: String): LiveData<ButtonText>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButtonText(buttonText: ButtonText)
    @Delete
    suspend fun deleteButtonText(buttonText: ButtonText)

    @Query("SELECT * FROM dnslocations ORDER BY name DESC")
    fun getDnsLocations(): LiveData<List<DnsLocation>>
    @Query("SELECT * FROM dnslocations WHERE response_id = :responseId")
    fun getLastDnsLocations(responseId: String): LiveData<List<DnsLocation>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDnsLocation(dnsLocation: DnsLocation)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDnsLocations(dnsLocations: List<DnsLocation>)
    @Delete
    suspend fun deleteDnsLocation(dnsLocation: DnsLocation)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArophixDnsResponse(buttonText: ButtonText, dnsLocations: List<DnsLocation>)

    /**
     * This query will tell Room to query both the [ButtonText] and [DnsLocation] tables and handle
     * the object mapping.
     */
    @Transaction
    @Query("SELECT * FROM arophix_button_texts WHERE id IN (SELECT DISTINCT(response_id) FROM dnslocations)")
    fun getArophixDnsResponses(): LiveData<List<ArophixDnsResponse>>

    @Query("DELETE FROM dnslocations")
    fun cleanDnsLocationsTable()

    @Query("DELETE FROM arophix_button_texts")
    fun cleanButtonTextsTable()
}
