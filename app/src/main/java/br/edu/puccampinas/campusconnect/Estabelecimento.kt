package br.edu.puccampinas.campusconnect

import java.io.Serializable

class Estabelecimento(
    val cnpj: String,
    val name: String,
    val photo: String,
    val description: String,
    val openingHours: String,
    val ownerId: String

) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 2511790574004449845L
    }
}
