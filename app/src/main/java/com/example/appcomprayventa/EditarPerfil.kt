package com.example.appcomprayventa

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.appcomprayventa.databinding.ActivityEditarPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditarPerfil : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
       setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)
        
        cargarInfo()

        binding.FABCambiarImg.setOnClickListener {
            selec_imagen_de()
        }
    }

    private fun cargarInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("CompraVenta/Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    val telefono ="${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"

                    binding.EtNombres.setText(nombres)
                    binding.EtFecha.setText(f_nac)
                    binding.EtTelefono.setText(telefono)

                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.imgPerfil)
                    }catch(e: Exception){
                        Toast.makeText(this@EditarPerfil,
                            "${e.message}",
                            Toast.LENGTH_SHORT).show()
                    }

                    try{
                        val codigo = codTelefono.replace("+","").toInt()
                        binding.SelectorCod.setCountryForPhoneCode(codigo)
                    }catch(e:Exception){
                        Toast.makeText(this@EditarPerfil,
                            "${e.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun selec_imagen_de(){
        val popupMenu = PopupMenu(this, binding.FABCambiarImg)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if(itemId == 1){
               //funcionalidad pa camara
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    concederPermisosCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else{
                    concederPermisosCamara.launch(arrayOf(
                        android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }
            }else if (itemId == 2){
                //funcionalidad para galeria
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    imagenGaleria()
                }else{
                    concederPermisosAlmacenamiento.launch(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private val concederPermisosCamara =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
            var concedidoTodos = true
            for (seConcede in resultado.values) {
                concedidoTodos = concedidoTodos && seConcede
            }

            if(concedidoTodos) {
                imagenCamara()
            } else{
                Toast.makeText(
                    this,
                    "El permiso de la cámara o almacenamiento se denegaron",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val concederPermisosAlmacenamiento =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { esConcedido ->
            if(esConcedido) {
                imagenGaleria()
            } else {
                Toast.makeText(this, "El permiso de almacenamiento se denegó",
                    Toast.LENGTH_SHORT).show()
            }
        }


}