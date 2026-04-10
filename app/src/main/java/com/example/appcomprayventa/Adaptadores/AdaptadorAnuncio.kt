package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.appcomprayventa.Anuncios.DetalleAnuncio
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

        // Llamamos a la nueva función para descargar TODAS las imágenes y armar el carrusel
        cargarCarrusel(modelo, holder)
        holder.itemView.setOnClickListener {
            // Creamos un Intent para abrir la nueva actividad (que crearemos en el Paso 2)
            val intent = android.content.Intent(context, DetalleAnuncio::class.java)

            // Pasamos todos los datos del anuncio a la nueva pantalla
            intent.putExtra("idAnuncio", modelo.idAnuncio)
            intent.putExtra("titulo", modelo.titulo)
            intent.putExtra("precio", modelo.precio)
            intent.putExtra("condicion", modelo.condicion)
            intent.putExtra("categoria", modelo.categoria)
            intent.putExtra("marca", modelo.marca)
            intent.putExtra("descripcion", modelo.descripcion)

            context.startActivity(intent)
        }
    }

    private fun cargarCarrusel(modelo: ModeloAnuncio, holder: HolderAnuncio) {
        val idAnuncio = modelo.idAnuncio

        // Vamos a la ruta exacta donde están las imágenes de ESTE anuncio
        val refImagenes = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Imagenes")

        // Ya NO usamos limitToFirst(1), queremos descargar todas las fotos
        refImagenes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaUrls = ArrayList<String>()

                if (snapshot.exists()) {
                    // Recorremos todas las imágenes encontradas en Firebase
                    for (ds in snapshot.children) {
                        val imagenUrl = ds.child("imagenUrl").value.toString()
                        listaUrls.add(imagenUrl)
                    }

                    // Inicializamos nuestro adaptador secundario y se lo pasamos al ViewPager2
                    try {
                        val adaptadorCarrusel = AdaptadorCarrusel(context, listaUrls)
                        holder.vpCarrusel.adapter = adaptadorCarrusel
                    } catch (e: Exception) {
                        // Prevenir crasheos si la vista se destruye antes de que termine de cargar
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores
            }
        })
    }

    // Clase interna que mantiene las vistas del item_anuncio.xml
    inner class HolderAnuncio(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Cambiamos el ImageView por el ViewPager2
        var vpCarrusel: ViewPager2 = itemView.findViewById(R.id.VP_carrusel)
        var tvTitulo: TextView = itemView.findViewById(R.id.Tv_titulo)
        var tvPrecio: TextView = itemView.findViewById(R.id.Tv_precio)
        var tvCategoria: TextView = itemView.findViewById(R.id.Tv_categoria)
        var tvCondicion: TextView = itemView.findViewById(R.id.Tv_condicion)
    }
}