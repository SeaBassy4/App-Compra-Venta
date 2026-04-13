package com.example.appcomprayventa.Anuncios

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Adaptadores.AdaptadorCarrusel
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityDetalleAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.example.appcomprayventa.Adaptadores.AdaptadorComentario
import com.example.appcomprayventa.Modelo.ModeloComentario

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAnuncioBinding
    private var idAnuncio = ""
    private var miReaccion = "" // Guardará "like", "dislike" o ""


    private lateinit var comentariosArrayList: ArrayList<ModeloComentario>
    private lateinit var adaptadorComentario: AdaptadorComentario

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

        // 4. Iniciar el sistema de Likes/Dislikes
        manejarReacciones()

        // 5. Iniciar el sistema de Comentarios
        binding.BtnEnviarComentario.setOnClickListener {
            validarYGuardarComentario()
        }
        cargarComentarios()
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

                    val adaptadorCarrusel = AdaptadorCarrusel(this@DetalleAnuncio, listaUrls)
                    binding.VPCarruselDetalle.adapter = adaptadorCarrusel
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error si es necesario
            }
        })
    }

    // ==========================================
    // SISTEMA DE LIKES Y DISLIKES
    // ==========================================

    private fun manejarReacciones() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val refReacciones = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Reacciones")

        // 1. Escuchar los clicks en los botones
        binding.BtnLike.setOnClickListener { reaccionar("like") }
        binding.BtnDislike.setOnClickListener { reaccionar("dislike") }

        // 2. Escuchar la base de datos en tiempo real
        refReacciones.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var likes = 0
                var dislikes = 0
                miReaccion = ""

                // Recorremos todas las reacciones de este anuncio
                for (ds in snapshot.children) {
                    val valor = ds.value.toString()

                    // Contamos los totales
                    if (valor == "like") likes++
                    else if (valor == "dislike") dislikes++

                    // Verificamos si el usuario actual ya reaccionó
                    if (ds.key == uid) {
                        miReaccion = valor
                    }
                }

                // Actualizar los textos de los contadores
                binding.TvLikesCount.text = "$likes"
                binding.TvDislikesCount.text = "$dislikes"

                // Actualizar visualmente los botones
                actualizarIconos()
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun reaccionar(nuevoTipo: String) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Reacciones").child(uid)

        if (miReaccion == nuevoTipo) {
            // Si el usuario hace clic en el mismo botón que ya había presionado, quitamos la reacción (Toggle)
            ref.removeValue()
        } else {
            // Si es diferente (o si no tenía ninguna), guardamos la nueva reacción
            ref.setValue(nuevoTipo)
        }
    }

    private fun actualizarIconos() {
        if (miReaccion == "like") {
            binding.BtnLike.setImageResource(R.drawable.ic_like_filled)
            binding.BtnDislike.setImageResource(R.drawable.ic_dislike_outline)
        } else if (miReaccion == "dislike") {
            binding.BtnLike.setImageResource(R.drawable.ic_like_outline)
            binding.BtnDislike.setImageResource(R.drawable.ic_dislike_filled)
        } else {
            // Estado neutro: ninguno está seleccionado
            binding.BtnLike.setImageResource(R.drawable.ic_like_outline)
            binding.BtnDislike.setImageResource(R.drawable.ic_dislike_outline)
        }
    }

    // ==========================================
    // SISTEMA DE COMENTARIOS
    // ==========================================

    private fun validarYGuardarComentario() {
        val textoComentario = binding.EtComentario.text.toString().trim()

        if (textoComentario.isEmpty()) {
            Toast.makeText(this, "El comentario no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        // Dividir el texto en base a espacios en blanco (incluyendo saltos de línea y tabulaciones)
        val cantidadPalabras = textoComentario.split("\\s+".toRegex()).size

        if (cantidadPalabras > 50) {
            Toast.makeText(this, "Tu comentario tiene $cantidadPalabras palabras. El límite es 50.", Toast.LENGTH_SHORT).show()
            return
        }

        guardarComentarioEnFirebase(textoComentario)
    }

    private fun guardarComentarioEnFirebase(comentario: String) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val tiempo = System.currentTimeMillis()

        // IMPORTANTE: Al usar el "uid" como llave final, Firebase automáticamente sobreescribe
        // si el usuario ya tenía un comentario anterior. Así garantizamos 1 por usuario.
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Comentarios").child(uid)

        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = uid
        hashMap["comentario"] = comentario
        hashMap["tiempo"] = tiempo

        ref.setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Comentario publicado exitosamente", Toast.LENGTH_SHORT).show()
                binding.EtComentario.setText("") // Limpiamos el EditText
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al comentar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarComentarios() {
        // Descomentar cuando tengas ModeloComentario y AdaptadorComentario listos

        comentariosArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Comentarios")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comentariosArrayList.clear()

                for (ds in snapshot.children) {
                    try {
                        val modelo = ds.getValue(ModeloComentario::class.java)
                        if (modelo != null) {
                            comentariosArrayList.add(modelo)
                        }
                    } catch (e: Exception) {
                        // Evitar fallos de parseo
                    }
                }

                // Configurar el adaptador para mostrar la lista
                adaptadorComentario = AdaptadorComentario(this@DetalleAnuncio, comentariosArrayList)
                binding.RVComentarios.adapter = adaptadorComentario
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores
            }
        })

    }
}