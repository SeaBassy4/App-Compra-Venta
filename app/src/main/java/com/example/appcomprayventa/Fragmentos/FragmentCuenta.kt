package com.example.appcomprayventa.Fragmentos

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.FragmentCuentaBinding
import com.example.appcomprayventa.Opciones_Login.Login_email
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.EditarPerfil

class FragmentCuenta : Fragment() {

    private var _binding: FragmentCuentaBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuentaBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarImagenPerfil()
        leerInfo()

        binding.BtnEditarPerfil.setOnClickListener {
            // Usamos requireContext() en lugar de mContext
            startActivity(Intent(requireContext(), EditarPerfil::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesionCompleta()
        }

        binding.TvPerfil.setOnClickListener {
            actualizarImagenPerfil()
        }
    }

    private fun cargarImagenPerfil() {
        val imagenUrl = "https://picsum.photos/200/300?random=${System.currentTimeMillis()}"
        Glide.with(this)
            .load(imagenUrl)
            .placeholder(R.drawable.img_perfil)
            .error(R.drawable.img_perfil)
            .circleCrop()
            .into(binding.TvPerfil)
    }

    private fun actualizarImagenPerfil() {
        val nuevaUrl = "https://picsum.photos/200/300?random=${System.currentTimeMillis()}"
        Glide.with(this)
            .load(nuevaUrl)
            .placeholder(R.drawable.img_perfil)
            .error(R.drawable.img_perfil)
            .circleCrop()
            .into(binding.TvPerfil)
        Toast.makeText(requireContext(), "¡Imagen actualizada!", Toast.LENGTH_SHORT).show()
    }

    private fun cerrarSesionCompleta() {
        Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show()
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            redirigirALogin()
        }
    }

    private fun redirigirALogin() {
        val intent = Intent(requireContext(), Login_email::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun leerInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("CompraVenta/Usuarios")
        val uid = auth.uid

        if (uid != null) {
            ref.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // =======================================================
                    // ESCUDO ANTI-CRASHEO: Evita el NullPointerException
                    if (!isAdded || _binding == null) return
                    // =======================================================

                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    var tiempo = "${snapshot.child("tiempo").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"

                    val cod_tel = "$codTelefono $telefono"

                    if (tiempo == "null") { tiempo = "0" }
                    val for_tiempo = Constantes.obtenerFecha(tiempo.toLong())

                    // Ahora es 100% seguro usar "binding"
                    binding.TvEmail.text = email
                    binding.TvNombres.text = nombres
                    binding.TvNacimiento.text = f_nac
                    binding.TvTelefono.text = cod_tel
                    binding.TvMiembro.text = for_tiempo

                    try {
                        Glide.with(requireContext())
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.TvPerfil)
                    } catch (e: Exception) {
                        // Opcional: manejar error de carga
                    }

                    if (proveedor == "Email") {
                        val esVerificado = auth.currentUser?.isEmailVerified == true
                        binding.TvEstadoCuenta.text = if (esVerificado) "Verificado" else "No Verificado"
                    } else {
                        binding.TvEstadoCuenta.text = "Verificado"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Protegemos también aquí por si acaso
                    if (!isAdded || _binding == null) return
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}