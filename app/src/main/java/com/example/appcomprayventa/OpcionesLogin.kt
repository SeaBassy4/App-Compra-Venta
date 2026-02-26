package com.example.appcomprayventa

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.appcomprayventa.Opciones_Login.Login_email
import com.example.appcomprayventa.databinding.ActivityOpcionesLoginBinding
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class OpcionesLogin : AppCompatActivity() {

    private lateinit var binding: ActivityOpcionesLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.IngresarGoogle.setOnClickListener {
            googleLogin()
        }

        binding.IngresarEmail.setOnClickListener {
            startActivity(Intent(this@OpcionesLogin, Login_email::class.java))
        }

    }

    private fun googleLogin() {
        println("Hola desde googleLogin!")
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ resultado ->
        println("entrando al google sign in arl")
        if(resultado.resultCode == RESULT_OK){
            println("result code ok")

            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)

                autenticacionGoogle(cuenta.idToken)
            }catch (e: Exception){
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun llenarInfoBD() {
        progressDialog.setMessage("Guardando Información")
        println("📝 Guardando usuario en BD:")

        val tiempo =  Constantes.obtenerTiempoDis()
        val emailUsuario = firebaseAuth.currentUser!!.email
        val uidUsuario = firebaseAuth.uid
        val nombreUsuario = firebaseAuth.currentUser?.displayName

        val hashMap = HashMap<String, Any>()
        hashMap["nombres"] = "${nombreUsuario}"
        hashMap["codigoTelefono"] = " "
        hashMap["telefono"] = " "
        hashMap["urlImagenPerfil"] = " "
        hashMap["proveedor"] = "Google"
        hashMap["escribiendo"] = " "
        hashMap["tiempo"] = tiempo
        hashMap["online"] = true
        hashMap["email"] = "${emailUsuario}"
        hashMap["uid"] = "${uidUsuario}"
        hashMap["fecha_nac"] = ""

        val ref = FirebaseDatabase.getInstance().getReference("CompraVenta").child("Usuarios")
        ref.child(uidUsuario!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                println("Usuario guardado en BD en teoria!!")
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }.addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(this, "No se registró debido a ${exception.message}",
                    Toast.LENGTH_SHORT)
                    .show()
            }
    }



    private fun autenticacionGoogle(idToken: String?) {
        println("metodo autenticacion google")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { resultadoAuth ->
                if(resultadoAuth.additionalUserInfo!!.isNewUser){
                    println("metodo autenticacion google: usuario es nuevo")

                    llenarInfoBD()
                }else{
                    println("metodo autenticacion google: usuario NO NUEVO, yendo directo a view")

                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun comprobarSesion() {
        if(firebaseAuth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }


}