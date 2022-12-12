package com.example.preferencesdatastorejava

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class Test : AppCompatActivity() {

    val dataStore: DataStore<Settings> = MultiProcessDataStoreFactory.create(
        serializer = SettingsSerializer(),
        produceFile = {
            File("${context.cacheDir.path}/myapp.preferences_pb")
        }
    )

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }
}

class SettingsSerializer  : Serializer<Settings> {

    override val defaultValue = Settings(lastUpdate = 0)

    override suspend fun readFrom(input: InputStream): Timer =
        try {
            Json.decodeFromString(
                Settings.serializer(), input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        output.write(
            Json.encodeToString(Settings.serializer(), t)
                .encodeToByteArray()
        )
    }
}

data class Settings(
    val lastUpdate: Long
)