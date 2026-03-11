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

class FragmentCuenta : Fragment() {

    // Variable para el binding (nullable)
    private var _binding: FragmentCuentaBinding? = null

    // Propiedad segura (non-null)
    private val binding get() = _binding!!

    // Firebase y Google SignIn
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // INFLAR CON VIEWBINDING
        _binding = FragmentCuentaBinding.inflate(inflater, container, false)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar la imagen aleatoria
        cargarImagenPerfil()


        leerInfo()

        // Configurar botón de cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesionCompleta()
        }

        // Opcional: Configurar clic en la imagen para actualizarla
        binding.TvPerfil.setOnClickListener {
            actualizarImagenPerfil()
        }
    }

    /**
     * Carga una imagen aleatoria usando Glide
     */
    private fun cargarImagenPerfil() {
        // URL que devuelve imagen aleatoria - añadimos timestamp para evitar caché
        val imagenUrl = "https://picsum.photos/200/300?random=${System.currentTimeMillis()}"

        Glide.with(this)
            .load(imagenUrl)
            .placeholder(R.drawable.img_perfil) // Mientras carga
            .error(R.drawable.img_perfil)       // Si hay error
            .circleCrop()                        // Opcional: hace la imagen circular
            // .centerCrop()                      // Alternativa: si no quieres circular
            .into(binding.TvPerfil)
    }

    /**
     * Actualiza la imagen con una nueva aleatoria
     */
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

    /**
     * Carga los datos del usuario desde Firebase (ejemplo)
     * Asumiendo que tienes una estructura de datos en Firebase Database
     */
    private fun cargarDatosUsuario() {
        val usuarioActual = auth.currentUser

        if (usuarioActual != null) {
            // Actualizar email
            binding.TvEmail.text = usuarioActual.email ?: "No disponible"

            // Aquí puedes cargar más datos desde Firebase Database
            // Por ejemplo, nombres, fecha de membresía, etc.
            val userId = usuarioActual.uid

            // Ejemplo (requiere tener Firebase Database configurada):
            /*
            val database = FirebaseDatabase.getInstance().reference
            database.child("usuarios").child(userId).get().addOnSuccessListener { snapshot ->
                binding.TvNombres.text = snapshot.child("nombres").value?.toString() ?: "Usuario"
                binding.TvMiembro.text = snapshot.child("fechaRegistro").value?.toString() ?: "Reciente"
            }
            */
        } else {
            // No hay usuario logueado, redirigir al login
            redirigirALogin()
        }
    }

    private fun cerrarSesionCompleta() {
        Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show()

        // Cerrar sesión de Firebase
        auth.signOut()

        // Cerrar sesión de Google específicamente
        googleSignInClient.signOut().addOnCompleteListener {
            // Opcional: mostrar mensaje cuando termina
        }

        // Redirigir al Login
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
        // LIMPIAR BINDING PARA EVITAR MEMORY LEAKS
        _binding = null
    }

    /*
    private fun leerInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object: ValueEventListener){
                override fun onDataChange(snapshot: DataSnapshot){
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    val tiempo = "${snapshot.child("tiempo").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"

                    val cod_tel = codTelefono + telefono

                    if (tiempo == "null"){
                        tiempo = "0"
                    }

                    val for_tiempo = Constantes.obtenerFecha(tiempo.toLong())

                    binding.TvEmail.text = email
                    binding.TvNombres.text = nombres
                    binding.TvNacimiento.text = f_nac
                    binding.TvTelefono.text = cod_tel
                    binding.TvMiembro.text = for_tiempo

                    try {
                        Glide.with(mContext)
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.TvPerfil)
                    }catch (e: Exception){
                        Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    if(proveedor == "Email"){
                        val esVerificado = firebaseAuth.currentUser!!.isEmailVerified
                        if(esVerificado){
                            binding.TvEstadoCuenta.text = "Verificado"
                        }else {
                            binding.TvEstadoCuenta.text = "No Verificado"
                        }
                    }else{
                        binding.TvEstadoCuenta.text = "Verificado"
                    }

                }
                override fun onCancelled(error: DatabaseError){

                }
            }
    }

     */

    private fun leerInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")

        // CORRECCIÓN 1: Usar auth.uid en lugar de firebaseAuth.uid
        ref.child("${auth.uid}")
            // CORRECCIÓN 4: Abrir la llave dentro del paréntesis
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"

                    // CORRECCIÓN 3: Cambiar val por var para poder reasignarlo
                    var tiempo = "${snapshot.child("tiempo").value}"

                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"

                    val cod_tel = codTelefono + telefono

                    if (tiempo == "null") {
                        tiempo = "0"
                    }

                    val for_tiempo = Constantes.obtenerFecha(tiempo.toLong())

                    binding.TvEmail.text = email
                    binding.TvNombres.text = nombres
                    binding.TvNacimiento.text = f_nac
                    binding.TvTelefono.text = cod_tel
                    binding.TvMiembro.text = for_tiempo

                    try {
                        // CORRECCIÓN 2: Usar requireContext() en lugar de mContext
                        Glide.with(requireContext())
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.TvPerfil)
                    } catch (e: Exception) {
                        // CORRECCIÓN 2: Usar requireContext() en el Toast también
                        Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    if (proveedor == "Email") {
                        // CORRECCIÓN 1: Usar auth.currentUser
                        // Usamos un safe call (?) por si es nulo
                        val esVerificado = auth.currentUser?.isEmailVerified == true

                        if (esVerificado) {
                            binding.TvEstadoCuenta.text = "Verificado"
                        } else {
                            binding.TvEstadoCuenta.text = "No Verificado"
                        }
                    } else {
                        binding.TvEstadoCuenta.text = "Verificado"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Aquí puedes agregar un Toast o un Log si falla la lectura de la base de datos
                }
            }) // CORRECCIÓN 4: El paréntesis cierra hasta acá abajo
    }
}