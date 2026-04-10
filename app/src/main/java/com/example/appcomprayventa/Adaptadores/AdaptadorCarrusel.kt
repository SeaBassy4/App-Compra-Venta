package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.R

class AdaptadorCarrusel(
    private val context: Context,
    private val listaUrls: ArrayList<String>
) : RecyclerView.Adapter<AdaptadorCarrusel.HolderCarrusel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCarrusel {
        val view = LayoutInflater.from(context).inflate(R.layout.item_imagen_carrusel, parent, false)
        return HolderCarrusel(view)
    }

    override fun getItemCount(): Int {
        return listaUrls.size
    }

    override fun onBindViewHolder(holder: HolderCarrusel, position: Int) {
        val urlImagen = listaUrls[position]

        try {
            Glide.with(context)
                .load(urlImagen)
                .placeholder(R.drawable.agregar_img)
                .into(holder.ivCarruselItem)
        } catch (e: Exception) {
            // Manejar error de carga
        }
    }

    inner class HolderCarrusel(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivCarruselItem: ImageView = itemView.findViewById(R.id.Iv_carrusel_item)
    }
}