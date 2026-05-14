package com.example.appcomprayventa.Modelos

class Usuario {

    var uid : String = ""
    var email : String = ""
    var nombres : String = ""
    var urlImagenPerfil : String = ""
    var codigoTelefono : Any? = ""
    var telefono : Any? = ""
    var proveedor : String = ""
    var escribiendo : String = ""
    var tiempo : Long = 0
    var online : Boolean = false
    var fecha_nac : Any? = ""
    var Bloqueados : Any? = null

    constructor()

    constructor(
        uid: String,
        email: String,
        nombres: String,
        urlImagenPerfil: String,
        codigoTelefono: Any?,
        telefono: Any?,
        proveedor: String,
        escribiendo: String,
        tiempo: Long,
        online: Boolean,
        fecha_nac: Any?,
        Bloqueados: Any? = null
    ) {
        this.uid = uid
        this.email = email
        this.nombres = nombres
        this.urlImagenPerfil = urlImagenPerfil
        this.codigoTelefono = codigoTelefono
        this.telefono = telefono
        this.proveedor = proveedor
        this.escribiendo = escribiendo
        this.tiempo = tiempo
        this.online = online
        this.fecha_nac = fecha_nac
        this.Bloqueados = Bloqueados
    }
}