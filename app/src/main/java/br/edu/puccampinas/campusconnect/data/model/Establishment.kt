package br.edu.puccampinas.campusconnect

import java.io.Serializable

class Establishment(
    var id: String? = null,
    var cnpj: String,
    var name: String,
    var description: String,
    var openingHours: String,
    var photo: String,
    var ownerId: String
) : Serializable {
    constructor(
        cnpj: String,
        name: String,
        description: String,
        openingHours: String,
        photo: String,
        ownerId: String
    ) : this(null, cnpj, name, description, openingHours, photo, ownerId)

    companion object {
        private const val serialVersionUID: Long = 2511790574004449845L
    }
}
