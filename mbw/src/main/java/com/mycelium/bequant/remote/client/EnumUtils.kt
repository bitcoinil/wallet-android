package com.mycelium.bequant.remote.client

import com.squareup.moshi.*
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

interface EnumWithValue<T> {
    val value: T
}

object EnumJsonAdapterFactory : JsonAdapter.Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type !is Class<*> || !type.isEnum) {
            return null
        }

        val constants = type.enumConstants?.mapNotNull { it as? EnumWithValue<*> } ?: return null
        val first = constants.firstOrNull()?.value ?: return null
        val valueAdapter = moshi.adapter<Any>(first::class.java)

        return object : JsonAdapter<EnumWithValue<*>>() {
            override fun fromJson(reader: JsonReader): EnumWithValue<*>? {
                if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
                val value = valueAdapter.fromJson(reader)
                return constants.firstOrNull { it.value == value }
                    ?: throw JsonDataException("Expected one of ${constants.map { it.value }} but was $value at path ${reader.path}")
            }

            override fun toJson(writer: JsonWriter, value: EnumWithValue<*>?) {
                if (value == null) {
                    writer.nullValue()
                } else {
                    val innerValue = value.value
                    valueAdapter.toJson(writer, innerValue)
                }
            }

        }
    }

}

object EnumRetrofitConverterFactory : Converter.Factory() {
    override fun stringConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<*, String>? {
        if (type is Class<*> && type.isEnum) {
            return Converter<Enum<*>, String> { value ->
                value.javaClass.getField(value.name).getAnnotation(Json::class.java)?.name ?: value.name
            }
        }
        return null
    }
}