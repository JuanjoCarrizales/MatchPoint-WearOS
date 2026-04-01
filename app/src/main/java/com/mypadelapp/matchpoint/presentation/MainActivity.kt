package com.mypadelapp.matchpoint.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.mypadelapp.matchpoint.R
import com.mypadelapp.matchpoint.logic.FirebaseManager
import com.mypadelapp.matchpoint.logic.PartidoPadel
import com.mypadelapp.matchpoint.ui.PagerAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import androidx.core.app.ActivityCompat

class MainActivity : FragmentActivity() {

    lateinit var partido: PartidoPadel
    private val handler = Handler(Looper.getMainLooper())
    private val LOCATION_PERMISSION_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        partido = PartidoPadel(this)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = PagerAdapter(this, partido)

        iniciarReloj()

        //Pedir permiso para la ubicación:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST)
        }

        //Login automatico a Firebase:
        FirebaseManager.loginAutomatico(
        {println("conectado")},
        {println("Firebase sin conexión")}
        )
    }

    private fun iniciarReloj() {
        val txtHora = findViewById<TextView>(R.id.txtHora)
        val txtFecha = findViewById<TextView>(R.id.txtFecha)

        val actualizarHora = object : Runnable {
            override fun run() {
                val ahora = Date()
                txtHora.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(ahora)
                txtFecha.text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(ahora)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(actualizarHora)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}