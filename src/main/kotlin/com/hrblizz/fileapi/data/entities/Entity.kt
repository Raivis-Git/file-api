package com.hrblizz.fileapi.data.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Entity(
    @Id val token: String,
    val filename: String,
    val size: Long,
    val contentType: String,
    val createTime: Instant,
    val meta: Map<String, Any>,
    val source: String,
    val expireTime: Instant?
) {
    constructor(
        token: String,
        filename: String
    )
        : this(token, filename, 0, "", Instant.now(), emptyMap(), "", null)
}
