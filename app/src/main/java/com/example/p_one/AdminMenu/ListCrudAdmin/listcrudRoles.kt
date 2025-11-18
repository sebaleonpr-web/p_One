package com.example.p_one.AdminMenu.ListCrudAdmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.p_one.AdminMenu.CrudAdmin.crudRoles
import com.example.p_one.AdminMenu.EditCrudAdmin.crudRolesEdit
import com.example.p_one.Models.Rol
import com.example.p_one.R
import com.example.p_one.listAlumnos
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.get

class listcrudRoles : AppCompatActivity() {

    private lateinit var lvRoles: ListView
    private lateinit var db: FirebaseFirestore

    private val listaRoles = mutableListOf<Rol>()
    private lateinit var adapterRoles: ArrayAdapter<Rol>

    private val mapaAdmins = mutableMapOf<String, String>()

    // Roles que NO se pueden editar ni eliminar
    private val rolesProtegidosPorNombre = setOf(
        "Alumno",
        "Profesor",
        "Administrador"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listcrud_roles)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        lvRoles = findViewById(R.id.lvRoles)

        // ADAPTADOR PERSONALIZADO USANDO item_rol.xml
        adapterRoles = object : ArrayAdapter<Rol>(this, R.layout.item_rol, listaRoles) {

            override fun getView(
                position: Int,
                convertView: android.view.View?,
                parent: android.view.ViewGroup
            ): android.view.View {

                val view = convertView ?: layoutInflater.inflate(R.layout.item_rol, parent, false)
                val rol = getItem(position)

                val tvNombre = view.findViewById<android.widget.TextView>(R.id.tvFilaNombreRol)
                val tvDescripcion = view.findViewById<android.widget.TextView>(R.id.tvFilaDescripcionRol)
                val tvNivel = view.findViewById<android.widget.TextView>(R.id.tvFilaNivelRol)
                val tvCreador = view.findViewById<android.widget.TextView>(R.id.tvFilaCreadorRol)

                if (rol != null) {

                    // Nombre
                    tvNombre.text = rol.nombreRol ?: "Sin nombre"

                    // Descripción
                    tvDescripcion.text = rol.descripcionRol ?: "Sin descripción"

                    // Nivel
                    tvNivel.text = rol.nivelAcceso?.toString() ?: "-"

                    // Creado por
                    val creadorId = rol.creadoPor
                    tvCreador.text = when {
                        creadorId.isNullOrEmpty() -> "Desconocido"
                        mapaAdmins.containsKey(creadorId) -> mapaAdmins[creadorId]
                        else -> creadorId
                    }
                }

                return view
            }
        }

        lvRoles.adapter = adapterRoles

        cargarMapaAdmins {
            cargarRoles()
            configurarEventosLista()
        }
    }

    private fun cargarMapaAdmins(onReady: () -> Unit) {
        db.collection("users")
            .whereEqualTo("rol", "Administrador")
            .get()
            .addOnSuccessListener { snap ->
                mapaAdmins.clear()

                for (doc in snap.documents) {
                    val idAdmin = doc.getString("idAdmin") ?: doc.id
                    val nombre = (doc.getString("nombre") ?: "") + " " +
                            (doc.getString("apellido") ?: "")

                    val label = nombre.trim().ifEmpty { "Admin sin nombre" }
                    mapaAdmins[idAdmin] = label
                }

                onReady()
            }
            .addOnFailureListener {
                onReady()
            }
    }

    private fun cargarRoles() {
        listaRoles.clear()
        adapterRoles.clear()

        db.collection("Roles")
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    mostrarAlerta("Aviso", "No hay roles registrados.")
                } else {
                    for (doc in snap.documents) {
                        val rol = doc.toObject(Rol::class.java) ?: continue

                        if (rol.idRol.isNullOrEmpty()) {
                            rol.idRol = doc.getString("idRol") ?: doc.id
                        }

                        listaRoles.add(rol)
                    }
                }

                adapterRoles.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                mostrarAlerta("Error", "Error al cargar roles: ${e.message}")
            }
    }


    private fun configurarEventosLista() {
        lvRoles.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->

                val rol = listaRoles[position]

                val opciones = arrayOf("Editar", "Eliminar", "Cancelar")

                AlertDialog.Builder(this)
                    .setTitle("Acciones del rol")
                    .setItems(opciones) { dialog, which ->
                        when (which) {
                            0 -> irAEditarRol(rol)
                            1 -> confirmarEliminarRol(rol, position)
                            else -> dialog.dismiss()
                        }
                    }.show()

                true
            }
    }

    private fun irAEditarRol(rol: Rol) {
        if (esRolProtegido(rol)) {
            mostrarAlerta("Aviso", "No puedes editar este rol.")
            return
        }

        val intent = Intent(this, crudRolesEdit::class.java)

        intent.putExtra("idRol", rol.idRol)
        intent.putExtra("nombreRol", rol.nombreRol)
        intent.putExtra("descripcionRol", rol.descripcionRol)
        intent.putExtra("nivelAcceso", rol.nivelAcceso)
        intent.putExtra("creadoPor", rol.creadoPor)

        val creadorNombre = if (!rol.creadoPor.isNullOrEmpty()) {
            mapaAdmins[rol.creadoPor] ?: "Admin sin nombre"
        } else {
            "Admin sin nombre"
        }
        intent.putExtra("creadorNombre", creadorNombre)

        if (rol.permisos != null) {
            intent.putStringArrayListExtra("permisos", ArrayList(rol.permisos!!))
        }

        startActivity(intent)
    }

    private fun esRolProtegido(rol: Rol): Boolean {
        val nombre = (rol.nombreRol ?: "").trim()
        return rolesProtegidosPorNombre.contains(nombre)
    }

    private fun confirmarEliminarRol(rol: Rol, position: Int) {

        if (esRolProtegido(rol)) {
            mostrarAlerta("Aviso", "No puedes eliminar este rol.")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar rol")
            .setMessage("¿Seguro que deseas eliminar el rol seleccionado?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarRol(rol, position)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarRol(rol: Rol, position: Int) {
        val id = rol.idRol ?: return

        db.collection("Roles")
            .document(id)
            .delete()
            .addOnSuccessListener {
                mostrarAlerta("Éxito", "Rol eliminado.")

                val item = adapterRoles.getItem(position)
                if (item != null) adapterRoles.remove(item)

                listaRoles.removeAt(position)
                adapterRoles.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                mostrarAlerta("Error", "Error al eliminar: ${e.message}")
            }
    }

    fun volvercurd(view: View){
        startActivity(Intent(this, crudRoles::class.java))
    }
    private fun mostrarAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .create()
            .show()
    }
}
