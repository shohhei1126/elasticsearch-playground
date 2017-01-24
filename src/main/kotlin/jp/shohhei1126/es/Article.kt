package jp.shohhei1126.es

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.ZonedDateTime

data class Article(
        val id: Long? = null,
        val title: String? = null,
        @JsonIgnore
        var titleHighlight: List<String>? = null,
        val tags: String? = null,
        @JsonIgnore
        var tagsHighlight: List<String>? = null,
        val body: String? = null,
        @JsonIgnore
        var bodyHighlight: List<String>? = null,
        val createdAt: ZonedDateTime? = null,
        val updatedAt: ZonedDateTime? = null)