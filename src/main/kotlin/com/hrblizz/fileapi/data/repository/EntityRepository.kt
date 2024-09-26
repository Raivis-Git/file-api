package com.hrblizz.fileapi.data.repository

import com.hrblizz.fileapi.data.entities.Entity
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface EntityRepository : MongoRepository<Entity, String> {

    fun findByTokenIn(tokens: List<String>): Optional<List<Entity>>

}