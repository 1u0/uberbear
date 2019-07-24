package am.royalbank.uberbear.frameworks.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object ObjectMappers {
    fun createApiObjectMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
}
