package com.sleekydz86.paperlens.infrastructure.web

import com.sleekydz86.paperlens.application.dto.LoginRequest
import com.sleekydz86.paperlens.application.dto.RegisterRequest
import com.sleekydz86.paperlens.application.usecase.AuthUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authUseCase: AuthUseCase) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest) =
        ResponseEntity.ok(authUseCase.login(request))

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest) =
        ResponseEntity.ok(authUseCase.register(request))
}