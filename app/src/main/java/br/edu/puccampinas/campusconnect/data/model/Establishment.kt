package br.edu.puccampinas.campusconnect.data.model


class Establishment {
    var id: String? = null
    var name: String? = null
    var description: String? = null
    var openingHours: String? = null
    var photo: String? = null
    var ownerId: String? = null

    constructor()

    constructor(name: String?, description: String?, openingHours: String?, photo: String?, ownerId: String?) {
        this.name = name
        this.description = description
        this.openingHours = openingHours
        this.photo = photo
        this.ownerId = ownerId
    }
}
