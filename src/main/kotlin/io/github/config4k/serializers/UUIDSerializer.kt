package io.github.config4k.serializers

import kotlinx.serialization.KSerializer
import java.util.UUID

/**
 * Serializer for [UUID]
 *
 * Usage example:
 * ```
 * @Serializable
 * data class ExampleUUID(
 *   @Serializable(UUIDSerializer::class)
 *   val uuid: UUID,
 * )
 * val config = ConfigFactory.parseString("uuid = b14240e7-acc0-4a19-bc5d-31a7901e36b0")
 * val exampleUUID: ExampleRegex = Hocon.decodeFromConfig(config)
 * val newConfig = Hocon.encodeToConfig(ExampleUUID.serializer(), exampleUUID)
 * ```
 */
public object UUIDSerializer : KSerializer<UUID> by stringSerializer(UUID::fromString, UUID::toString)
