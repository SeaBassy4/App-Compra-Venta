package com.example.appcomprayventa.Anuncios

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Adaptadores.AdaptadorImagenSeleccionada
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.ModeloImagenSeleccionada
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityCrearAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class CrearAnuncio : AppCompatActivity() {
    private lateinit var binding : ActivityCrearAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    private var imagenUri : Uri?=null

    private lateinit var imagenSelecArrayList : ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSel : AdaptadorImagenSeleccionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        // Configurar Spinners/Dropdowns
        val adaptadorCat = ArrayAdapter(this, R.layout.item_categoria, Constantes.categorias)
        binding.Categoria.setAdapter(adaptadorCat)

        val adaptadorCon = ArrayAdapter(this, R.layout.item_condicion, Constantes.condiciones)
        binding.Condicion.setAdapter(adaptadorCon)

        // Inicializar lista y adaptador para el RecyclerView
        imagenSelecArrayList = ArrayList()
        cargarImagenes()

        // 1. Botón para agregar imagen (Abre el menú Cámara/Galería)
        binding.agregarImg.setOnClickListener {
            if (imagenSelecArrayList.size >= 3) {
                // Si ya hay 3 imágenes en la lista, mostramos un aviso y evitamos que siga
                Toast.makeText(this, "Solo puedes agregar un máximo de 3 imágenes", Toast.LENGTH_SHORT).show()
            } else {
                // Si tiene menos de 3, le permitimos abrir el menú normalmente
                selec_imagen_de()
            }
        }

        // 2. Botón para crear el anuncio final
        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
        }
    }

    private fun cargarImagenes() {
        adaptadorImagenSel = AdaptadorImagenSeleccionada(this, imagenSelecArrayList)
        binding.RVImagenes.adapter = adaptadorImagenSel
    }

    // =====================================================================
    // LÓGICA DE SELECCIÓN DE IMÁGENES (Adaptada de EditarPerfil)
    // =====================================================================

    private fun selec_imagen_de(){
        val popupMenu = PopupMenu(this, binding.agregarImg)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if(itemId == 1){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    concederPermisosCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else{
                    concederPermisosCamara.launch(arrayOf(
                        android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }
            }else if (itemId == 2){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    imagenGaleria()
                }else{
                    concederPermisosAlmacenamiento.launch(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private val concederPermisosCamara =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
            var concedidoTodos = true
            for (seConcede in resultado.values) {
                concedidoTodos = concedidoTodos && seConcede
            }
            if(concedidoTodos) {
                imagenCamara()
            } else{
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
            }
        }

    private val concederPermisosAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            if(esConcedido) {
                imagenGaleria()
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun imagenCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)
    }

    private val resultadoCamara_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if(resultado.resultCode == RESULT_OK){
                agregarImagenALista()
            }else{
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun imagenGaleria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if(resultado.resultCode == RESULT_OK) {
                val data = resultado.data
                imagenUri = data!!.data
                agregarImagenALista()
            }else{
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    // =====================================================================
    // GUARDADO LOCAL DE IMÁGENES SELECCIONADAS
    // =====================================================================

    private fun agregarImagenALista() {
        // ?.let asegura que el bloque de código solo se ejecute si imagenUri NO es nulo.
        // Además, te da una variable segura llamada "uriSegura" que puedes usar.
        imagenUri?.let { uriSegura ->
            val modelo = ModeloImagenSeleccionada(
                id = "${System.currentTimeMillis()}",
                imagenUri = uriSegura, // Usamos la variable segura aquí
                imagenUrl = null,
                deInternet = false
            )
            imagenSelecArrayList.add(modelo)
            adaptadorImagenSel.notifyDataSetChanged()
        }
    }

    // =====================================================================
    // SUBIDA A FIREBASE (DATOS + IMÁGENES)
    // =====================================================================

    private fun validarDatos() {
        val marca = binding.EtMarca.text.toString().trim()
        val categoria = binding.Categoria.text.toString().trim()
        val condicion = binding.Condicion.text.toString().trim()
        val precio = binding.EtPrecio.text.toString().trim()
        val titulo = binding.EtTitulo.text.toString().trim()
        val descripcion = binding.EtDescripcion.text.toString().trim()

        if (titulo.isEmpty() || precio.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
        } else if (imagenSelecArrayList.isEmpty()) {
            Toast.makeText(this, "Debe agregar al menos una imagen", Toast.LENGTH_SHORT).show()
        } else {
            subirAnuncioYObtenerUrls(marca, categoria, condicion, precio, titulo, descripcion)
        }
    }

    private fun subirAnuncioYObtenerUrls(marca: String, categoria: String, condicion: String, precio: String, titulo: String, descripcion: String) {
        progressDialog.setMessage("Subiendo anuncio e imágenes...")
        progressDialog.show()

        val tiempo = System.currentTimeMillis()
        val uidUsuario = firebaseAuth.uid!!
        val idAnuncio = "$tiempo" // Usamos el timestamp como ID único del anuncio

        // 1. Guardar la info básica del anuncio primero
        val hashMap = HashMap<String, Any>()
        hashMap["idAnuncio"] = idAnuncio
        hashMap["uid"] = uidUsuario
        hashMap["marca"] = marca
        hashMap["categoria"] = categoria
        hashMap["condicion"] = condicion
        hashMap["precio"] = precio
        hashMap["titulo"] = titulo
        hashMap["descripcion"] = descripcion
        hashMap["tiempo"] = tiempo

        val refAnuncio = FirebaseDatabase.getInstance().getReference("Anuncios")
        refAnuncio.child(idAnuncio).setValue(hashMap).addOnSuccessListener {
            // 2. Una vez guardado el anuncio, subimos las imágenes
            subirImagenes(idAnuncio)
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Error al crear anuncio: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subirImagenes(idAnuncio: String) {
        // Recorremos la lista de imágenes seleccionadas
        for (i in 0 until imagenSelecArrayList.size) {
            val modeloImagen = imagenSelecArrayList[i]
            val nombreImagen = modeloImagen.id // Usamos el ID que generamos al seleccionarla
            val rutaImagen = "Anuncios/$idAnuncio/$nombreImagen"

            val storageReference = FirebaseStorage.getInstance().getReference(rutaImagen)
            storageReference.putFile(modeloImagen.imagenUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // 3. Obtenemos la URL de descarga
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val urlImagenCargada = uri.toString()
                        guardarUrlImagenEnBD(idAnuncio, modeloImagen.id, urlImagenCargada, i == imagenSelecArrayList.size - 1)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Fallo al subir una imagen", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarUrlImagenEnBD(idAnuncio: String, idImagen: String, urlImagen: String, esLaUltima: Boolean) {
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = idImagen
        hashMap["imagenUrl"] = urlImagen

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio).child("Imagenes")
        ref.child(idImagen).setValue(hashMap).addOnSuccessListener {
            if (esLaUltima) {
                progressDialog.dismiss()
                Toast.makeText(this, "Anuncio publicado con éxito", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad tras publicar
            }
        }
    }
}