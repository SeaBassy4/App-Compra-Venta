package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appcomprayventa.Modelo.ModeloComentario
import com.example.appcomprayventa.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class AdaptadorComentario(
    private val context: Context,
    private val comentariosArrayList: ArrayList<ModeloComentario>
) : RecyclerView.Adapter<AdaptadorComentario.HolderComentario>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        // Inflamos el diseño que acabamos de crear
        val view = LayoutInflater.from(context).inflate(R.layout.item_comentario, parent, false)
        return HolderComentario(view)
    }

    override fun getItemCount(): Int {
        return comentariosArrayList.size
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        // Obtenemos los datos del comentario en esta posición
        val modelo = comentariosArrayList[position]

        val uid = modelo.uid
        val comentario = modelo.comentario
        val tiempo = modelo.tiempo

        // 1. Mostrar el texto del comentario
        holder.tvComentario.text = comentario

        // 2. Formatear el tiempo (Long) a una fecha legible (ej. 13/04/2026)
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = tiempo
        val fechaFormateada = DateFormat.format("dd/MM/yyyy", calendario).toString()
        holder.tvFecha.text = fechaFormateada

        // 3. Obtener el nombre del usuario desde Firebase usando su UID
        cargarInformacionUsuario(uid, holder)
    }

    private fun cargarInformacionUsuario(uid: String, holder: HolderComentario) {

        val ref = FirebaseDatabase.getInstance().getReference("CompraVenta/Usuarios")

        ref.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombres").value.toString()
                    holder.tvUsuario.text = nombre
                } else {
                    holder.tvUsuario.text = "Usuario Anónimo"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Si falla por problemas de red
                holder.tvUsuario.text = "Usuario"
            }
        })
    }
    
    inner class HolderComentario(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUsuario: TextView = itemView.findViewById(R.id.Tv_usuario_comentario)
        var tvFecha: TextView = itemView.findViewById(R.id.Tv_fecha_comentario)
        var tvComentario: TextView = itemView.findViewById(R.id.Tv_texto_comentario)
    }
}