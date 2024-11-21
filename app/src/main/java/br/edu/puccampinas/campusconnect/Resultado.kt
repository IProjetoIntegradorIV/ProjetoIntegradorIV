package br.edu.puccampinas.campusconnect

import java.io.Serializable

class Resultado(
    val mensagem: String,
    val erros: List<String>? = null
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 8556072105244890025L
    }
}
