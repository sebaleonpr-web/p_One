package com.example.p_one

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.p_one.crudAdmin.crudAdministrador
import com.example.p_one.crudAdmin.crudAlumno
import com.example.p_one.crudAdmin.crudProfesor

class menuProfesor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_profesor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun curdalum(view: View){
        startActivity(Intent(this, listAlumnos::class.java))
    }
    fun curdCurso(view: View){
        startActivity(Intent(this, listCursosProfe::class.java))
    }
    fun curdPuntuacion(view: View){
        startActivity(Intent(this, listPuntuacionProfe::class.java))
    }
}