package com.example.appcomprayventa.Fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.FragmentCuentaBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.appcomprayventa.OpcionesLogin
import com.example.appcomprayventa.Opciones_Login.Login_email
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class FragmentCuenta : Fragment() {

    // Variable para el binding (nullable)
    private var _binding: FragmentCuentaBinding? = null

    // Propiedad segura (non-null)
    private val binding get() = _binding!!

    // Añadir GoogleSignInClient
    private lateinit var googleSignInClient: GoogleSignInClient



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ✅ INFLAR CON VIEWBINDING
        _binding = FragmentCuentaBinding.inflate(inflater, container, false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ USAR BINDING PARA EL BOTÓN
        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesionCompleta()
        }
    }

    private fun cerrarSesionCompleta() {
        // Mostrar indicador de progreso (opcional)
        Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show()

        // 1. Cerrar sesión de Firebase (funciona para todos los métodos)
        FirebaseAuth.getInstance().signOut()

        // 2. Cerrar sesión de Google específicamente
        googleSignInClient.signOut().addOnCompleteListener {
            // Este callback se ejecuta cuando Google cierra sesión
            // No necesitas hacer nada especial aquí, solo redirigir
        }

        // 3. Opcional: Revocar acceso (eliminar permiso completamente)
        // googleSignInClient.revokeAccess().addOnCompleteListener { ... }

        // 4. Redirigir al Login
        redirigirALogin()
    }


    private fun redirigirALogin() {
        val intent = Intent(requireContext(), Login_email::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ✅ LIMPIAR BINDING PARA EVITAR MEMORY LEAKS
        _binding = null
    }


}