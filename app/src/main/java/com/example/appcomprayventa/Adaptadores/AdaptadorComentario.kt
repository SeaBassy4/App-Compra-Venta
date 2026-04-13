package com.example.appcomprayventa.Adaptadores

import android.app.AlertDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appcomprayventa.Modelo.ModeloComentario
import com.example.appcomprayventa.Modelo.ModeloRespuesta
import com.example.appcomprayventa.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

class AdaptadorComentario(
    private val context: Context,
    private val comentariosArrayList: ArrayList<ModeloComentario>,
    private val idAnuncio: String // NUEVO: Necesitamos saber el ID del anuncio
) : RecyclerView.Adapter<AdaptadorComentario.HolderComentario>() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val miUid = firebaseAuth.uid ?: ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comentario, parent, false)
        return HolderComentario(view)
    }

    override fun getItemCount(): Int = comentariosArrayList.size

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentariosArrayList[position]

        val uidComentario = modelo.uid
        holder.tvComentario.text = modelo.comentario

        val calendario = Calendar.getInstance()
        calendario.timeInMillis = modelo.tiempo
        holder.tvFecha.text = DateFormat.format("dd/MM/yyyy", calendario).toString()

        cargarInformacionUsuario(uidComentario, holder)

        // 1. Manejar Likes del Comentario
        manejarLikesComentario(uidComentario, holder)

        // 2. Manejar Clic en "Responder"
        holder.btnResponder.setOnClickListener {
            mostrarDialogoRespuesta(uidComentario)
        }

        // 3. Cargar las respuestas anidadas de este comentario
        cargarRespuestas(uidComentario, holder)
    }

    private fun cargarInformacionUsuario(uid: String, holder: HolderComentario) {
        val ref = FirebaseDatabase.getInstance().getReference("CompraVenta/Usuarios")
        ref.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.tvUsuario.text = if (snapshot.exists()) snapshot.child("nombres").value.toString() else "Usuario Anónimo"
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun manejarLikesComentario(uidComentario: String, holder: HolderComentario) {
        val refLikes = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Comentarios").child(uidComentario).child("Likes")

        var yaDioLike = false

        // Escuchar cambios en los likes en tiempo real
        refLikes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.tvLikes.text = snapshot.childrenCount.toString() // Cuenta total de likes
                yaDioLike = snapshot.hasChild(miUid)

                if (yaDioLike) {
                    holder.btnLike.setImageResource(R.drawable.ic_like_filled)
                } else {
                    holder.btnLike.setImageResource(R.drawable.ic_like_outline)
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        })

        // Acción al hacer clic en el botón de Like
        holder.btnLike.setOnClickListener {
            if (miUid.isNotEmpty()) {
                if (yaDioLike) {
                    refLikes.child(miUid).removeValue() // Quitar Like
                } else {
                    refLikes.child(miUid).setValue("like") // Dar Like
                }
            } else {
                Toast.makeText(context, "Inicia sesión para dar like", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoRespuesta(uidComentarioPadre: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Responder al comentario")

        val input = EditText(context)
        input.hint = "Escribe tu respuesta (Máx 50 palabras)..."
        builder.setView(input)

        builder.setPositiveButton("Enviar") { _, _ ->
            val texto = input.text.toString().trim()
            if (texto.isNotEmpty()) {
                guardarRespuestaEnFirebase(uidComentarioPadre, texto)
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun guardarRespuestaEnFirebase(uidComentarioPadre: String, respuesta: String) {
        if (miUid.isEmpty()) return
        val tiempo = System.currentTimeMillis()

        // Guardamos la respuesta usando el miUid para limitar a 1 respuesta por usuario por comentario
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Comentarios").child(uidComentarioPadre)
            .child("Respuestas").child(miUid)

        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = miUid
        hashMap["respuesta"] = respuesta
        hashMap["tiempo"] = tiempo

        ref.setValue(hashMap).addOnSuccessListener {
            Toast.makeText(context, "Respuesta enviada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarRespuestas(uidComentarioPadre: String, holder: HolderComentario) {
        val respuestasArrayList = ArrayList<ModeloRespuesta>()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Comentarios").child(uidComentarioPadre).child("Respuestas")

        // Configurar el RecyclerView anidado
        holder.rvRespuestas.layoutManager = LinearLayoutManager(context)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                respuestasArrayList.clear()
                for (ds in snapshot.children) {
                    try {
                        val modelo = ds.getValue(ModeloRespuesta::class.java)
                        if (modelo != null) {
                            respuestasArrayList.add(modelo)
                        }
                    } catch (e: Exception) { }
                }

                val adaptadorRespuesta = AdaptadorRespuesta(context, respuestasArrayList, idAnuncio, uidComentarioPadre)
                holder.rvRespuestas.adapter = adaptadorRespuesta
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    inner class HolderComentario(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUsuario: TextView = itemView.findViewById(R.id.Tv_usuario_comentario)
        var tvFecha: TextView = itemView.findViewById(R.id.Tv_fecha_comentario)
        var tvComentario: TextView = itemView.findViewById(R.id.Tv_texto_comentario)

        // Elementos nuevos
        var btnLike: ImageButton = itemView.findViewById(R.id.Btn_like_comentario)
        var tvLikes: TextView = itemView.findViewById(R.id.Tv_likes_comentario)
        var btnResponder: TextView = itemView.findViewById(R.id.Btn_responder_comentario)
        var rvRespuestas: RecyclerView = itemView.findViewById(R.id.RV_Respuestas)
    }
}