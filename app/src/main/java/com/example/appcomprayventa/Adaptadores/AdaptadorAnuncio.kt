package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdaptadorAnuncio(
    private val context: Context,
    private val anunciosArrayList: ArrayList<ModeloAnuncio>
) : RecyclerView.Adapter<AdaptadorAnuncio.HolderAnuncio>() {

    // 1. Inflar (cargar) el diseño de item_anuncio.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {
        val view = LayoutInflater.from(context).inflate(R.layout.item_anuncio, parent, false)
        return HolderAnuncio(view)
    }

    // 2. ¿Cuántos elementos hay en total?
    override fun getItemCount(): Int {
        return anunciosArrayList.size
    }

    // 3. Conectar los datos a la vista para una posición específica
    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {
        val modelo = anunciosArrayList[position]

        // Setear datos en la interfaz
        holder.tvTitulo.text = modelo.titulo
        holder.tvPrecio.text = "$${modelo.precio}"
        holder.tvCategoria.text = modelo.categoria
        holder.tvCondicion.text = modelo.condicion

        // Llamamos a la función para descargar y mostrar la primera imagen
        cargarPrimeraImagen(modelo, holder)
    }

    private fun cargarPrimeraImagen(modelo: ModeloAnuncio, holder: HolderAnuncio) {
        val idAnuncio = modelo.idAnuncio

        // Vamos a la ruta exacta donde están las imágenes de ESTE anuncio en específico
        val refImagenes = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Imagenes")

        // Usamos limitToFirst(1) para descargar SOLO la primera foto (ahorra datos y tiempo)
        refImagenes.limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Extraemos la primera imagen de la lista de resultados
                    val primeraImagenSnapshot = snapshot.children.first()
                    val imagenUrl = primeraImagenSnapshot.child("imagenUrl").value.toString()

                    // Usamos Glide para pintar la imagen en el ImageView
                    try {
                        Glide.with(context)
                            .load(imagenUrl)
                            .placeholder(R.drawable.agregar_img) // Imagen por defecto mientras carga
                            .into(holder.ivAnuncio)
                    } catch (e: Exception) {
                        // Evita que la app falle si el fragmento se cierra mientras carga la imagen
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Aquí puedes manejar errores si lo deseas
            }
        })
    }

    // Clase interna que mantiene las vistas del item_anuncio.xml
    inner class HolderAnuncio(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivAnuncio: ImageView = itemView.findViewById(R.id.Iv_anuncio)
        var tvTitulo: TextView = itemView.findViewById(R.id.Tv_titulo)
        var tvPrecio: TextView = itemView.findViewById(R.id.Tv_precio)
        var tvCategoria: TextView = itemView.findViewById(R.id.Tv_categoria)
        var tvCondicion: TextView = itemView.findViewById(R.id.Tv_condicion)
    }
}