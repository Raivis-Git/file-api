package com.hrblizz.fileapi.controller.dto

import com.hrblizz.fileapi.data.entities.Entity

data class FileMetadataResponse(val files: Map<String, Entity>)