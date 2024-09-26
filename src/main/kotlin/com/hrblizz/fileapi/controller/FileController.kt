package com.hrblizz.fileapi.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hrblizz.fileapi.controller.dto.FileMetadataResponse
import com.hrblizz.fileapi.controller.exception.InternalServerErrorException
import com.hrblizz.fileapi.controller.exception.NotFoundException
import com.hrblizz.fileapi.data.entities.Entity
import com.hrblizz.fileapi.data.repository.EntityRepository
import com.hrblizz.fileapi.library.log.LogItem
import com.hrblizz.fileapi.library.log.Logger
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.util.*
import com.hrblizz.fileapi.controller.dto.FileMetadataRequest as FileMetadataRequest

@RestController
internal class FileController(private val entityRepository: EntityRepository) {
    private val logger = Logger()
    private val objectMapper = jacksonObjectMapper()
    private val uploadDir = "uploads"

    init {
        Files.createDirectories(Paths.get(uploadDir))
    }

    @PostMapping("/files")
    fun uploadFile(
        @RequestParam("name") name: String,
        @RequestParam("contentType") contentType: String,
        @RequestParam("meta") meta: String,
        @RequestParam("source") source: String,
        @RequestParam("expireTime", required = false) expireTime: String?,
        @RequestParam("content") content: MultipartFile
    ): ResponseEntity<Map<String, String>> {
        try {
            val token = UUID.randomUUID().toString()
            val filePath = Paths.get(uploadDir, token)
            Files.write(filePath, content.bytes)
            val entity = Entity(
                token = token,
                filename = name,
                size = content.size,
                contentType = contentType,
                createTime = Instant.now(),
                meta = objectMapper.readValue(meta),
                source = source,
                expireTime = expireTime?.let { Instant.parse(it) }
            )
            entityRepository.save(entity)

            return ResponseEntity(mapOf("token" to token), HttpStatus.CREATED)
        } catch (e: Exception) {
            logger.error(LogItem("Error uploading file\n$e"))
            throw InternalServerErrorException("Error uploading file")
        }
    }

    @PostMapping("/files/metas")
    fun getFileMetadata(@RequestBody request: FileMetadataRequest): ResponseEntity<FileMetadataResponse> {
        try {
            val files = entityRepository.findByTokenIn(request.tokens).orElseThrow { NotFoundException("File not found") }
            val filesMap = files.associateBy { it.token }
            return ResponseEntity.ok(FileMetadataResponse(filesMap))
        } catch (e: Exception) {
            logger.error(LogItem("Error retrieving file metadata\n$e"))
            throw InternalServerErrorException("Error retrieving file metadata")
        }
    }

    @GetMapping("/file/{token}")
    fun downloadFile(@PathVariable token: String): ResponseEntity<UrlResource> {
        try {
            val entity = entityRepository.findById(token).orElseThrow { NotFoundException("File not found") }

            val file = Paths.get(uploadDir).resolve(token).toFile()
            val resource = UrlResource(file.toURI())

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${entity.filename}\"")
                .header("X-Filename", entity.filename)
                .header("X-Filesize", entity.size.toString())
                .header("X-CreateTime", entity.createTime.toString())
                .header(HttpHeaders.CONTENT_TYPE, entity.contentType)
                .body(resource)
        } catch (e: Exception) {
            logger.error(LogItem("Error downloading file\n$e"))
            throw InternalServerErrorException("Error downloading file")
        }
    }

    @DeleteMapping("/file/{token}")
    fun deleteFile(@PathVariable token: String): ResponseEntity<Unit> {
        try {
            val fileMetadata = entityRepository.findById(token).orElseThrow { NotFoundException("File not found") }

            Files.deleteIfExists(Paths.get(uploadDir, token))
            entityRepository.delete(fileMetadata)

            return ResponseEntity.noContent().build()
        } catch (e: Exception) {
            logger.error(LogItem("Error deleting file\n$e"))
            throw InternalServerErrorException("Error deleting file")
        }
    }
}