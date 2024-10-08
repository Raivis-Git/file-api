package com.hrblizz.fileapi.controller.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class InternalServerErrorException(
    message: String
) : RuntimeException(message)
