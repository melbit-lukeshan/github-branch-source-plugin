package org.jenkinsci.plugins.azure_devops_repo_branch_source.util.gson

import com.google.gson.*
import org.jenkinsci.plugins.azure_devops_repo_branch_source.util.support.Constants
import org.jenkinsci.plugins.azure_devops_repo_branch_source.util.support.Constants.ZONE_ID_DEFAULT
import org.jenkinsci.plugins.azure_devops_repo_branch_source.util.support.LogUtil
import java.lang.reflect.Type
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KClass

/**
 * Pass the desired format to parse-from and format-to
 * Pass [null] to parse-from/format-to epoch milliseconds
 */
class GsonTypeAdapterOffsetDateTime(deserializerFormat: String? = Constants.DATE_TIME_FORMAT_AZURE,
                                    serializerFormat: String? = Constants.DATE_TIME_FORMAT_AZURE)
    : GsonTypeAdapter, JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime> {

    private val formatterForDeserializer: DateTimeFormatter? by lazy {
        deserializerFormat?.let {
            DateTimeFormatter.ofPattern(it, Locale.US)
        }
    }
    private val formatterForSerializer: DateTimeFormatter? by lazy {
        serializerFormat?.let {
            DateTimeFormatter.ofPattern(it, Locale.US)
        }
    }
    private val additionalDateFormattersForDeserializerSet by lazy {
        mutableListOf<DateTimeFormatter>()
    }

    fun addAdditionalDateFormatForDeserializer(additionalDateFormatForDeserialize: String): GsonTypeAdapterOffsetDateTime {
        additionalDateFormattersForDeserializerSet.add(DateTimeFormatter.ofPattern(additionalDateFormatForDeserialize, Locale.US))
        return this
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OffsetDateTime? =
            try {
                formatterForDeserializer?.parse(json.asString, OffsetDateTime::from)
                        ?: OffsetDateTime.ofInstant(Instant.ofEpochMilli(json.asLong), ZONE_ID_DEFAULT)
            } catch (e: Exception) {
                additionalDateFormattersForDeserializerSet.forEach {
                    try {
                        it.parse(json.asString, OffsetDateTime::from)
                    } catch (ex: Exception) {
                    }
                }
                LogUtil.logThrowable(e)
                null
            }

    override fun serialize(src: OffsetDateTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            formatterForSerializer?.let {
                JsonPrimitive(it.format(src))
            } ?: JsonPrimitive(src.toInstant().toEpochMilli())

    override fun getTargetClass(): KClass<*> {
        return OffsetDateTime::class
    }
}