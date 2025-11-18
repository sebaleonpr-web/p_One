package com.example.p_one.Main.Bienvenidas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.p_one.Main.menuAdmin
import com.example.p_one.Models.Users
import com.example.p_one.R
import com.google.firebase.firestore.FirebaseFirestore

class bienvenidaScreenAdmin : AppCompatActivity() {

    private lateinit var tvBienvenidaAdmin: TextView
    private lateinit var btnIrMenuAdmin: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida_screen_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        tvBienvenidaAdmin = findViewById(R.id.tvBienvenidaAdmin)
        btnIrMenuAdmin = findViewById(R.id.btnIrMenuAdmin)

        val uidAuth = intent.getStringExtra("uidAuth")

        if (uidAuth != null) {
            db.collection("users")
                .document(uidAuth)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val admin = doc.toObject(Users::class.java)

                        val nombre = admin?.nombre ?: ""
                        val apellido = admin?.apellido ?: ""

                        tvBienvenidaAdmin.text = "Bienvenido, Admin $nombre $apellido"

                        btnIrMenuAdmin.setOnClickListener {
                            val intentMenu = Intent(this, menuAdmin::class.java)
                            intentMenu.putExtra("uidAuth", uidAuth)
                            startActivity(intentMenu)
                        }

                    } else {
                        tvBienvenidaAdmin.text = "Bienvenido, Administrador"
                    }
                }
        } else {
            tvBienvenidaAdmin.text = "Bienvenido, Administrador"
        }
    }
}
