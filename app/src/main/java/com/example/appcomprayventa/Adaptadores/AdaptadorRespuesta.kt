package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appcomprayventa.Modelo.ModeloRespuesta
import com.example.appcomprayventa.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

class AdaptadorRespuesta(
    private val context: Context,
    private val respuestasArrayList: ArrayList<ModeloRespuesta>,
    private val idAnuncio: String,
    private val uidComentarioPadre: String
) : RecyclerView.Adapter<AdaptadorRespuesta.HolderRespuesta>() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val miUid = firebaseAuth.uid ?: ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRespuesta {
        val view = LayoutInflater.from(context).inflate(R.layout.item_respuesta, parent, false)
        return HolderRespuesta(view)
    }

    override fun getItemCount(): Int = respuestasArrayList.size

    override fun onBindViewHolder(holder: HolderRespuesta, position: Int) {
        val modelo = respuestasArrayList[position]

        holder.tvRespuesta.text = modelo.respuesta
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = modelo.tiempo
        holder.tvFecha.text = " • " + DateFormat.format("dd/MM/yyyy", calendario).toString()

        cargarInformacionUsuario(modelo.uid, holder)
        manejarLikes(modelo.uid, holder)
    }

    private fun cargarInformacionUsuario(uid: String, holder: HolderRespuesta) {
        val ref = FirebaseDatabase.getInstance().getReference("CompraVenta/Usuarios")
        ref.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.tvUsuario.text = if (snapshot.exists()) snapshot.child("nombres").value.toString() else "Usuario Anónimo"
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun manejarLikes(uidRespuesta: String, holder: HolderRespuesta) {
        val refLikes = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Comentarios").child(uidComentarioPadre)
            .child("Respuestas").child(uidRespuesta).child("Likes")

        var yaDioLike = false

        refLikes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.tvLikes.text = snapshot.childrenCount.toString()
                yaDioLike = snapshot.hasChild(miUid)
                if (yaDioLike) {
                    holder.btnLike.setImageResource(R.drawable.ic_like_filled)
                } else {
                    holder.btnLike.setImageResource(R.drawable.ic_like_outline)
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        })

        holder.btnLike.setOnClickListener {
            if (miUid.isNotEmpty()) {
                if (yaDioLike) refLikes.child(miUid).removeValue()
                else refLikes.child(miUid).setValue("like")
            }
        }
    }

    inner class HolderRespuesta(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUsuario: TextView = itemView.findViewById(R.id.Tv_usuario_respuesta)
        var tvFecha: TextView = itemView.findViewById(R.id.Tv_fecha_respuesta)
        var tvRespuesta: TextView = itemView.findViewById(R.id.Tv_texto_respuesta)
        var btnLike: ImageButton = itemView.findViewById(R.id.Btn_like_respuesta)
        var tvLikes: TextView = itemView.findViewById(R.id.Tv_likes_respuesta)
    }
}