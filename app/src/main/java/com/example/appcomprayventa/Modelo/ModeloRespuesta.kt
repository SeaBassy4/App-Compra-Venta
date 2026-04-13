package com.example.appcomprayventa.Modelo

class ModeloRespuesta {
    var uid: String = ""
    var respuesta: String = ""
    var tiempo: Long = 0

    constructor()
    constructor(uid: String, respuesta: String, tiempo: Long) {
        this.uid = uid
        this.respuesta = respuesta
        this.tiempo = tiempo
    }
}