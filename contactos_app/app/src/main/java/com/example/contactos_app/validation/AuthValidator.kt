package com.example.contactos_app.validation

import android.util.Patterns

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Errors(val fieldErrors: Map<String, String>) : ValidationResult()
}

object AuthValidator {
    fun validateContact(
        name: String,
        phone: String,
        email: String
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (name.isBlank()) {
            errors["name"] = "Nombre requerido"
        } else if (name.length < 2) {
            errors["name"] = "Mínimo 2 caracteres"
        } else if (name.length > 20) {
            errors["name"] = "Máximo 20 caracteres"
        }

        if (phone.isBlank()) {
            errors["phone"] = "Teléfono requerido"
        } else if (!phone.all { it.isDigit() }) {
            errors["phone"] = "Ingrese solo números"
        } else if (phone.length < 10) {
            errors["phone"] = "Mínimo 10 caracteres"
        } else if (phone.length > 13) {
            errors["phone"] = "Máximo 13 caracteres"
        }

        if (email.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            errors["email"] = "Correo no valido"
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Errors(errors)
        }
    }
}
