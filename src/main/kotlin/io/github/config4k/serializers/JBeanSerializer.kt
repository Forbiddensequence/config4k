package io.github.config4k.serializers

import com.typesafe.config.Config
import com.typesafe.config.ConfigBeanFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.hocon.HoconDecoder
import kotlin.reflect.KClass

/**
 * Serializer for Java Bean objects. Supports only decoding of objects from [Config].
 * [JBeanSerializer] using [com.typesafe.config.ConfigBeanFactory.create] to decode Java Bean objects.
 * To create a [JBeanSerializer], use the method [jBeanSerializer]
 * Usage example:
 *
 * Java Bean code
 * ```
 * public class TestJavaBean {
 *  private String name;
 *  private int age;
 *  // getters and setters
 * }
 * ```
 * Kotlin code
 * ```
 * object TestJavaBeanSerializer : KSerializer<TestJavaBean> by JBeanSerializer.jBeanSerializer()
 *
 * @Serializable
 * data class ExampleJavaBean(
 *  @Serializable(with = TestJavaBeanSerializer::class)
 *  val bean: TestJavaBean
 * )
 * val config = ConfigFactory.parseString("""
 *  bean: {
 *      name = Alex
 *      age = 24
 *  }
 * """.trimIndent())
 * val exampleJavaBean: ExampleJavaBean = Hocon.decodeFromConfig(config)
 * ```
 */
public class JBeanSerializer<T>(
    private val clazz: Class<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("hocon.java.bean", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T =
        if (decoder is HoconDecoder) {
            decoder.decodeConfigValue { conf, path ->
                ConfigBeanFactory.create(conf.getConfig(path), clazz)
            }
        } else {
            throw SerializationException("This class can be decoded only by Hocon format")
        }

    override fun serialize(
        encoder: Encoder,
        value: T,
    ): Unit = throw SerializationException("JBeanSerializer is only used for deserialization")

    public companion object {
        /**
         * Creates a [JBeanSerializer] using a Java Bean type.
         * @return [JBeanSerializer]
         */
        public inline fun <reified T> jBeanSerializer(): JBeanSerializer<T> = JBeanSerializer(T::class.java)
    }
}

@SerialInfo
public annotation class JBeanClassHolder(
    val clazz: KClass<*>,
)
