package io.github.config4k.serializers

import io.github.config4k.Config4k
import io.github.config4k.render
import io.github.config4k.toConfig
import io.kotest.matchers.should
import io.mockk.mockk
import kotlinx.serialization.Contextual
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.hocon.encodeToConfig
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URL
import java.util.UUID

class MapAsListSerializerTest {
    private companion object {
        val simpleMap = mapOf("foo" to "bar")
        val ordinaryMap = mapOf(5 to "foo", 6 to "bar")
        val mapWithNested = mapOf(5 to NestedConf(nested = "bar"))
        val mapWithObjects =
            mapOf(UUID.fromString("b14240e7-acc0-4a19-bc5d-31a7901e36b0") to URL("https://www.example.com"))
    }

    @Serializable
    data class NestedConf(
        val nested: String,
    )

    @Nested
    inner class SimpleTest {
        @Test
        fun checkDefaultDecoding() {
            @Serializable
            data class Conf(
                val map: Map<String, String>,
            )

            val test: Conf =
                Config4k.decodeFromConfig("map = {foo = bar}".toConfig())

            assertThat(test.map).isEqualTo(simpleMap)

            assertThrows<MissingFieldException> { Config4k.decodeFromConfig<Conf>("foo = bar".toConfig()) }
                .should { assertThat(it.missingFields).contains("map") }
        }

        @Test
        fun checkMapAsObjectDecoding() {
            @Serializable
            data class Conf(
                val map: Map<Int, String>,
            )

            val test: Conf =
                Config4k.decodeFromConfig("map = {5 = foo, 6 = bar}".toConfig())

            assertThat(test.map).isEqualTo(ordinaryMap)

            assertThrows<MissingFieldException> { Config4k.decodeFromConfig<Conf>("foo = bar".toConfig()) }
                .should { assertThat(it.missingFields).contains("map") }
        }

        @Test
        fun checkMapAsListDecodingWithoutNested() {
            @Serializable
            data class Conf(
                @Serializable(with = MapAsListSerializer::class) val map: Map<Int, String>,
            )

            val test: Conf =
                Config4k.decodeFromConfig("map = [{key = 5, value = foo},{key = 6, value = bar}]".toConfig())

            assertThat(test.map).isEqualTo(ordinaryMap)

            assertThrows<MissingFieldException> { Config4k.decodeFromConfig<Conf>("foo = bar".toConfig()) }
                .should { assertThat(it.missingFields).contains("map") }
        }

        @Test
        fun checkDecodingWithNested() {
            @Serializable
            data class Conf(
                @Serializable(with = MapAsListSerializer::class) val map: Map<Int, NestedConf>,
            )

            val test: Conf =
                Config4k.decodeFromConfig("map = [{key = 5, value = {\"nested\" = \"bar\"}}]".toConfig())

            assertThat(test.map).isEqualTo(mapWithNested)

            assertThrows<MissingFieldException> { Config4k.decodeFromConfig<Conf>("value = bar".toConfig()) }
                .should { assertThat(it.missingFields).contains("map") }
        }

        @Test
        fun checkDecodingWithObject() {
            @Serializable
            data class Conf(
                @Serializable(with = MapAsListSerializer::class) val map: Map<@Contextual UUID, @Contextual URL>,
            )

            val test: Conf =
                Config4k.decodeFromConfig(
                    "map = [{key = b14240e7-acc0-4a19-bc5d-31a7901e36b0, value = \"https://www.example.com\"}]".toConfig(),
                )

            assertThat(test.map).isEqualTo(mapWithObjects)

            assertThrows<MissingFieldException> { Config4k.decodeFromConfig<Conf>("value = bar".toConfig()) }
                .should { assertThat(it.missingFields).contains("map") }
        }

        @Test
        fun checkEncoding() {
            @Serializable
            data class Conf(
                @Serializable(with = MapAsListSerializer::class) val map: Map<Int, NestedConf>,
            )

            var test = Config4k.encodeToConfig(Conf(mapWithNested))
            assertThat(test.render()).isEqualTo("map=[{key=5,value{nested=bar}}]")
            test = Config4k.encodeToConfig(Conf(mapOf(6 to NestedConf(nested = "foo"))))
            assertThat(test.render()).isEqualTo("map=[{key=6,value{nested=foo}}]")
            test = Config4k.encodeToConfig(Conf(emptyMap()))
            assertThat(test.render()).isEqualTo("map=[]")
        }
    }

    @Test
    fun checkIncorrectEncoder() {
        assertThatThrownBy { MapAsListSerializer(String.serializer(), String.serializer()).serialize(mockk(), mockk()) }
            .isInstanceOf(SerializationException::class.java)
            .hasMessage("This class can be encoded only by Hocon format")
    }
}
