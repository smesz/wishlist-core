package com.wishlist.core.platform

import org.springframework.core.convert.converter.Converter

internal const val SEPARATOR = ","

class StringSetWritingConverter : Converter<Set<String>, String> {

    override fun convert(parameter: Set<String>): String {
        return parameter.joinToString(separator = SEPARATOR)
    }
}

class StringSetReadingConverter : Converter<String, Set<String>> {

    override fun convert(parameter: String): Set<String> {
        return when (parameter.isBlank()) {
            true -> mutableSetOf()
            false -> parameter.split(SEPARATOR).toMutableSet()
        }
    }
}