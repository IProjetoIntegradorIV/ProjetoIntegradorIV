package br.edu.puccampinas.campusconnect.data.model

data class User(
    val name: String,
    val email: String,
    val password: String,
    val establishmentOwner: Boolean,
    val photo : String? = null,
    val id : String? = null,
    )

