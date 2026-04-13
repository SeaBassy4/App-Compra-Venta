package com.example.appcomprayventa.Modelo

class ModeloComentario {
    var uid: String = ""
    var comentario: String = ""
    var tiempo: Long = 0

    // Constructor vacío requerido por Firebase Realtime Database
    constructor()

    // Constructor con parámetros
    constructor(uid: String, comentario: String, tiempo: Long) {
        this.uid = uid
        this.comentario = comentario
        this.tiempo = tiempo
    }
}