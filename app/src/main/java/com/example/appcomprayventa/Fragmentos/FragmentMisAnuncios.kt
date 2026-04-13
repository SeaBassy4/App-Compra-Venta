package com.example.appcomprayventa.Fragmentos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcomprayventa.Adaptadores.AdaptadorAnuncio
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.databinding.FragmentMisAnunciosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentMisAnuncios : Fragment() {

    // Usando ViewBinding para no tener que usar findViewById
    private lateinit var binding: FragmentMisAnunciosBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var anunciosArrayList: ArrayList<ModeloAnuncio>
    private lateinit var adaptadorAnuncio: AdaptadorAnuncio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMisAnunciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        cargarMisAnuncios()
    }

    private fun cargarMisAnuncios() {
        anunciosArrayList = ArrayList()

        // Obtener el ID del usuario actual para descargar solo SUS anuncios
        val uidActual = firebaseAuth.uid

        // Referencia a la base de datos "Anuncios"
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")

        // Buscamos donde el hijo "uid" sea igual al ID de nuestro usuario
        /*ref.orderByChild("uid").equalTo(uidActual)*/
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // =======================================================
                // VALIDACIÓN DE SEGURIDAD PARA EVITAR CRASHEOS (ZOMBIE LISTENER)
                if (!isAdded) return
                // =======================================================

                // Limpiamos la lista antes de agregar nuevos datos
                anunciosArrayList.clear()

                // Recorremos todos los anuncios devueltos
                for (ds in snapshot.children) {
                    try {
                        val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                        if (modeloAnuncio != null) {
                            anunciosArrayList.add(modeloAnuncio)
                        }
                    } catch (e: Exception) {
                        // Manejar posible error al parsear los datos
                    }
                }

                // Configurar el adaptador con la lista llena
                adaptadorAnuncio = AdaptadorAnuncio(requireContext(), anunciosArrayList)
                binding.RVMisAnuncios.adapter = adaptadorAnuncio
            }

            override fun onCancelled(error: DatabaseError) {
                // También protegemos aquí porque usas requireContext() para el Toast
                if (!isAdded) return

                Toast.makeText(requireContext(), "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}