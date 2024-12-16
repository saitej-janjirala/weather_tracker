package com.saitejajanjirala.weather_tracker.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.saitejajanjirala.weather_tracker.SimplifiedWeatherResultProto
import java.io.InputStream
import java.io.OutputStream

object SimplifiedWeatherResultSerializer : Serializer<SimplifiedWeatherResultProto> {
    override val defaultValue: SimplifiedWeatherResultProto = SimplifiedWeatherResultProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SimplifiedWeatherResultProto {
        try {
            return SimplifiedWeatherResultProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("No Saved City.", exception)
        }
    }

    override suspend fun writeTo(t: SimplifiedWeatherResultProto, output: OutputStream) {
        t.writeTo(output)
    }
}
