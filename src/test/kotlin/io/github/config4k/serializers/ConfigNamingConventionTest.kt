package io.github.config4k.serializers

import io.github.config4k.Config4k
import io.github.config4k.Config4kWithCamelCase
import io.github.config4k.toConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.decodeFromConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Serializable
data class TestConf(
    val complexAge: Int,
    val complexName: String
)


@Serializable
data class TestCaseConf(
    val `kebab-case`: String,
    val camelCase: String,
    val snake_case: String
)

class ConfigNamingConventionTest {

    @Test
    fun testCamelCaseEncode() {
        val test: TestConf =
            Config4kWithCamelCase.decodeFromConfig(
                "{complexAge = 24, complexName = \"Alex\"}".toConfig(),
            )
        assertThat(test).isEqualTo(TestConf(complexName = "Alex", complexAge = 24))
    }

    @Test
    fun testHyphenNameEncode() {
        val test: TestConf =
            Config4k.decodeFromConfig(
                "{complex-age = 24, complex-name = \"Alex\"}".toConfig(),
            )
        assertThat(test).isEqualTo(TestConf(complexName = "Alex", complexAge = 24))
    }

    @Test
    fun testMixEncode() {
        @Serializable
        data class OuterClass(
            val testConf: TestConf
        )

        val test: OuterClass =
            Config4k.decodeFromConfig(
                " test-conf {complex-age = 24, complex-name = \"Alex\"}".toConfig(),
            )
        assertThat(test).isEqualTo(OuterClass(TestConf(complexName = "Alex", complexAge = 24)))
    }

    @Test
    fun testConfigNamingEncode() {
        @Serializable
        data class OuterClass(
            @SerialName("testConf")
            val testConf: TestCaseConf
        )

        val test: OuterClass =
            Config4kWithCamelCase.decodeFromConfig(
                " testConf {kebab-case = \"test\", snake_case = \"test\", camelCase = \"test\"}".toConfig(),
            )
        assertThat(test).isEqualTo(OuterClass(TestCaseConf(`kebab-case` = "test", camelCase = "test", snake_case = "test")))
    }

}
