package com.example.appcomprayventa.Anuncios // O donde hayas puesto tu clase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Adaptadores.AdaptadorCarrusel
import com.example.appcomprayventa.databinding.ActivityDetalleAnuncioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAnuncioBinding
    private var idAnuncio = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Obtener los datos pasados desde el Adaptador
        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""
        val titulo = intent.getStringExtra("titulo")
        val precio = intent.getStringExtra("precio")
        val condicion = intent.getStringExtra("condicion")
        val categoria = intent.getStringExtra("categoria")
        val marca = intent.getStringExtra("marca")
        val descripcion = intent.getStringExtra("descripcion")

        // 2. Colocar los datos en los TextViews
        binding.TvTituloDetalle.text = titulo
        binding.TvPrecioDetalle.text = "$$precio"
        binding.TvCondicionDetalle.text = "Condición: $condicion"
        binding.TvCategoriaDetalle.text = "Categoría: $categoria"
        binding.TvMarcaDetalle.text = "Marca: $marca"
        binding.TvDescripcionDetalle.text = descripcion

        // 3. Cargar las imágenes del carrusel
        cargarImagenesCarrusel()
    }

    private fun cargarImagenesCarrusel() {
        val refImagenes = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Imagenes")

        refImagenes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaUrls = ArrayList<String>()

                if (snapshot.exists()) {
                    for (ds in snapshot.children) {
                        val imagenUrl = ds.child("imagenUrl").value.toString()
                        listaUrls.add(imagenUrl)
                    }

                    // Reutilizamos el adaptador de carrusel que creaste en el paso anterior
                    val adaptadorCarrusel = AdaptadorCarrusel(this@DetalleAnuncio, listaUrls)
                    binding.VPCarruselDetalle.adapter = adaptadorCarrusel
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error
            }
        })
    }
}