package com.bancoseguro.app.data.database

import androidx.room.TypeConverter
import com.bancoseguro.app.data.models.TransactionStatus

class Converters {
    @TypeConverter
    fun fromTransactionStatus(status: TransactionStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus {
        return TransactionStatus.valueOf(value)
    }
}
