package com.example.appcomprayventa.Fragmentos

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcomprayventa.Adaptadores.AdaptadorUsuario
import com.example.appcomprayventa.Modelos.Usuario
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentChats : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private var usuarioAdaptador: AdaptadorUsuario? = null
    private var usuarioLista: List<Usuario>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentChatsBinding.inflate(inflater, container, false)

        binding.RVUsuarios.setHasFixedSize(true)
        binding.RVUsuarios.layoutManager = LinearLayoutManager(mContext)

        usuarioLista = ArrayList()

        listarUsuarios()

        return binding.root
    }

    private fun listarUsuarios() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = FirebaseDatabase.getInstance().reference.child("CompraVenta/Usuarios").orderByChild("nombres")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Aquí va la lógica para procesar los datos
                (usuarioLista as ArrayList<Usuario>).clear()

                for (sn in snapshot.children) {
                    val usuario : Usuario? = sn.getValue(Usuario::class.java)

                    // Filtramos para no mostrarnos a nosotros mismos
                    if (!(usuario!!.uid).equals(firebaseUser)) {
                        (usuarioLista as ArrayList<Usuario>).add(usuario)
                    }
                }

                // Si la lista está vacía, mostramos el mensaje y ocultamos el RecyclerView
                if ((usuarioLista as java.util.ArrayList<Usuario>).isEmpty()) {
                    binding.tvSinUsuarios.visibility = View.VISIBLE
                    binding.RVUsuarios.visibility = View.GONE

                // Si hay más usuarios, ocultamos el mensaje y mostramos la lista
                } else {
                    binding.tvSinUsuarios.visibility = View.GONE
                    binding.RVUsuarios.visibility = View.VISIBLE

                    //actualizamos el adaptador

                    usuarioAdaptador = AdaptadorUsuario(mContext, usuarioLista!!)
                    binding.RVUsuarios.adapter = usuarioAdaptador
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores
                TODO("Not yet implemented")

                Log.e("FirebaseError", "Error al leer usuarios: ${error.message}")
                Toast.makeText(
                    mContext,
                    "Error al cargar datos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}