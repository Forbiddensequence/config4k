package io.github.config4k.serializers

import io.github.config4k.Config4k
import io.github.config4k.TestJavaBean
import io.github.config4k.toConfig
import io.mockk.mockk
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.hocon.decodeFromConfig
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

object TestJavaBeanSerializer : KSerializer<TestJavaBean> by JBeanSerializer.jBeanSerializer()

class JavaBeanSerializerTest {
    @Serializable
    private data class NestJavaBean(
        @Serializable(with = TestJavaBeanSerializer::class)
        val person: TestJavaBean,
        @Serializable(with = TestJavaBeanSerializer::class)
        val person2: TestJavaBean? = null,
    )

    private companion object {
        val personComparator = nullsLast(compareBy(TestJavaBean::getName, TestJavaBean::getAge))
        val defaultPerson: TestJavaBean =
            TestJavaBean().apply {
                age = 20
                name = "foo"
            }
    }

    @Nested
    inner class SimpleTest {
        @Test
        fun checkDecoding() {
            @Serializable
            data class Conf(
                @Serializable(with = TestJavaBeanSerializer::class)
                val person: TestJavaBean,
            )

            val test: Conf = Config4k.decodeFromConfig("person = { name = \"foo\", age = 20 }".toConfig())
            assertThat(test.person).usingComparator(personComparator).isEqualTo(defaultPerson)

            val nestedTest: NestJavaBean =
                Config4k.decodeFromConfig("{ person { name = \"foo\", age = 20 } }".toConfig())
            assertThat(nestedTest.person).usingComparator(personComparator).isEqualTo(defaultPerson)
            assertThat(nestedTest.person2).usingComparator(personComparator).isEqualTo(null)

            val thrown =
                catchThrowableOfType(MissingFieldException::class.java) { Config4k.decodeFromConfig<Conf>("foo = bar".toConfig()) }
            assertThat(thrown.missingFields).contains("person")
        }

        @Test
        fun checkIncorrectDecoder() {
            assertThatThrownBy { TestJavaBeanSerializer.deserialize(mockk()) }
                .isInstanceOf(SerializationException::class.java)
                .hasMessage("This class can be decoded only by Hocon format")
        }

        @Test
        fun checkNullableDecoding() {
            @Serializable
            data class Conf(
                @Serializable(with = TestJavaBeanSerializer::class)
                val person: TestJavaBean? = null,
            )

            val test: Conf = Config4k.decodeFromConfig("foo = bar".toConfig())
            assertThat(test.person).isNull()
        }

        @Test
        fun checkDefaultDecoding() {
            @Serializable
            data class Conf(
                @Serializable(with = TestJavaBeanSerializer::class)
                val person: TestJavaBean = defaultPerson,
            )

            val test: Conf =
                Config4k.decodeFromConfig("person = { name = \"foo\", age = 20 }".toConfig())
            assertThat(test.person).usingComparator(personComparator).isEqualTo(defaultPerson)

            val default: Conf = Config4k.decodeFromConfig("foo = bar".toConfig())
            assertThat(default.person).usingComparator(personComparator).isEqualTo(defaultPerson)
        }

        @Test
        fun checkEncoding() {
            assertThatThrownBy { TestJavaBeanSerializer.serialize(mockk(), mockk()) }
                .isInstanceOf(SerializationException::class.java)
                .hasMessage("JBeanSerializer is only used for deserialization")
        }
    }
}
