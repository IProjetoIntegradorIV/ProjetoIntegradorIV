package br.edu.puccampinas.campusconnect.data.model

data class Product(
    val id: String,
    val establishmentId: String,
    val name: String,
    val description: String,
    val evaluation: String,
    val photo: String
)
