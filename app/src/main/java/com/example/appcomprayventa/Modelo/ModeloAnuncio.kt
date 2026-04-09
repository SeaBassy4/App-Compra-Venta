package com.example.appcomprayventa.Modelo

class ModeloAnuncio {
    var idAnuncio: String = ""
    var uid: String = ""
    var marca: String = ""
    var categoria: String = ""
    var condicion: String = ""
    var precio: String = ""
    var titulo: String = ""
    var descripcion: String = ""
    var tiempo: Long = 0

    // Constructor vacío requerido por Firebase
    constructor()

    // Constructor con parámetros
    constructor(idAnuncio: String, uid: String, marca: String, categoria: String, condicion: String, precio: String, titulo: String, descripcion: String, tiempo: Long) {
        this.idAnuncio = idAnuncio
        this.uid = uid
        this.marca = marca
        this.categoria = categoria
        this.condicion = condicion
        this.precio = precio
        this.titulo = titulo
        this.descripcion = descripcion
        this.tiempo = tiempo
    }
}