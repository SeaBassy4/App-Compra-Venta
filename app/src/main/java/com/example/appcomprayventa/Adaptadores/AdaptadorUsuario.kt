package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Intent
import android.widget.Toast
import com.example.appcomprayventa.Chat.ChatActivity
import com.example.appcomprayventa.Modelos.Usuario
import com.example.appcomprayventa.R

class AdaptadorUsuario(
    private val context: Context,
    private val listaUsuarios: List<Usuario>,
    private val mapaNoLeidos: Map<String, Int> = emptyMap()
) : RecyclerView.Adapter<AdaptadorUsuario.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario: Usuario = listaUsuarios[position]
        holder.uid.text = usuario.uid
        holder.email.text = usuario.email
        holder.nombres.text = usuario.nombres

        Glide.with(context)
            .load(usuario.urlImagenPerfil)
            .placeholder(R.drawable.ic_imagen_perfil)
            .into(holder.imagen)

        // Actualizar badge de no leídos
        val count = mapaNoLeidos[usuario.uid] ?: 0
        if (count > 0) {
            holder.noLeidos.visibility = View.VISIBLE
            holder.noLeidos.text = count.toString()
        } else {
            holder.noLeidos.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uid", usuario.uid)
            Toast.makeText(
                context,
                "Has seleccionado al usuario: ${usuario.nombres}",
                Toast.LENGTH_SHORT
            ).show()
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uid: TextView = itemView.findViewById(R.id.item_uid)
        val email: TextView = itemView.findViewById(R.id.item_email)
        val nombres: TextView = itemView.findViewById(R.id.item_nombre)
        val imagen: ImageView = itemView.findViewById(R.id.item_imagen)
        val noLeidos: TextView = itemView.findViewById(R.id.item_noleidos)
    }
}