package io.github.config4k.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.hocon.HoconDecoder
import kotlinx.serialization.hocon.HoconEncoder

/**
 * Base function to create StringSerializer.
 * It should be used when hocon configuration value presented as string,
 * and it has simple cast to string by toString method
 *
 * @param decode Function to decode value from string
 * @param encode Function to encode value from toString, by default it is toString method
 * @param nameOverride Optional name overriding, by default it is class qualifiedName
 */
public inline fun <reified T> stringSerializer(
    crossinline decode: (String) -> T,
    crossinline encode: (T) -> String = { it.toString() },
    nameOverride: String? = null,
): KSerializer<T> =
    object : KSerializer<T> {
        override val descriptor = PrimitiveSerialDescriptor(nameOverride ?: T::class.qualifiedName!!, PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder) =
            if (decoder is HoconDecoder) {
                decode(decoder.decodeString())
            } else {
                throw SerializationException("This class can be decoded only by Hocon format")
            }

        override fun serialize(
            encoder: Encoder,
            value: T,
        ) = if (encoder is HoconEncoder) {
            encoder.encodeString(encode(value))
        } else {
            throw SerializationException("This class can be encoded only by Hocon format")
        }
    }
