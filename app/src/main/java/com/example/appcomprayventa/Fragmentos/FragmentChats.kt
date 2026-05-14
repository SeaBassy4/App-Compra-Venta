package com.example.appcomprayventa.Fragmentos

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appcomprayventa.Adaptadores.AdaptadorUsuario
import com.example.appcomprayventa.Modelos.Usuario
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
    private var usuarioLista: MutableList<Usuario> = mutableListOf()
    private var mapaNoLeidos: MutableMap<String, Int> = mutableMapOf()
    private var usuarioAdaptador: AdaptadorUsuario? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listarUsuarios()
        escucharNoLeidos()

        binding.etBuscarUsuario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarUsuario(s.toString().lowercase())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun escucharNoLeidos() {
        val miUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("CompraVenta/NoLeidos").child(miUid)
        
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null || !isAdded) return
                mapaNoLeidos.clear()
                for (ds in snapshot.children) {
                    try {
                        val count = (ds.value as? Long)?.toInt() ?: 0
                        mapaNoLeidos[ds.key!!] = count
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error al procesar no leídos: ${e.message}")
                    }
                }
                actualizarAdaptador()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun listarUsuarios() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val reference = FirebaseDatabase.getInstance().reference.child("CompraVenta/Usuarios").orderByChild("nombres")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null || !isAdded) return
                usuarioLista.clear()
                for (sn in snapshot.children) {
                    try {
                        val usuario : Usuario? = sn.getValue(Usuario::class.java)
                        if (usuario != null && usuario.uid != firebaseUser) {
                            usuarioLista.add(usuario)
                        } else if (usuario == null) {
                            Log.w("FirebaseError", "Usuario nulo en snapshot: ${sn.key}")
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error al convertir usuario ${sn.key}: ${e.message}")
                    }
                }
                Log.d("FirebaseDebug", "Usuarios cargados: ${usuarioLista.size}")
                actualizarAdaptador()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error al leer usuarios: ${error.message}")
            }
        })
    }

    private fun buscarUsuario(usuario : String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val reference = FirebaseDatabase.getInstance().reference
            .child("CompraVenta/Usuarios")
            .orderByChild("nombres")
            .startAt(usuario)
            .endAt(usuario + "\uf8ff")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null || !isAdded) return
                usuarioLista.clear()
                for (ss in snapshot.children) {
                    try {
                        val user : Usuario? = ss.getValue(Usuario::class.java)
                        if (user != null && user.uid != firebaseUser) {
                            usuarioLista.add(user)
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error al buscar usuario: ${e.message}")
                    }
                }
                actualizarAdaptador()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarAdaptador() {
        if (_binding == null || !isAdded) return
        
        if (usuarioLista.isEmpty()) {
            binding.tvSinUsuarios.visibility = View.VISIBLE
            binding.rvUsuarios.visibility = View.GONE
        } else {
            binding.tvSinUsuarios.visibility = View.GONE
            binding.rvUsuarios.visibility = View.VISIBLE

            val currentContext = context ?: mContext
            usuarioAdaptador = AdaptadorUsuario(currentContext, usuarioLista, mapaNoLeidos)
            binding.rvUsuarios.adapter = usuarioAdaptador
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}