package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitconnect.services.LocalPrefsService

class SplashActivity : AppCompatActivity() {
    private lateinit var localPrefsService: LocalPrefsService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        localPrefsService = LocalPrefsService(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if(localPrefsService.getIsAdmin()){
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            }
           else if(localPrefsService.getIsDoctor()){
                startActivity(Intent(this, DoctorDashboardActivity::class.java))
                finish()
            }
            else{
                if(localPrefsService.getPatientId()!=null){
                    startActivity(Intent(this, PatientHomeActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this, OnBoardActicityOne::class.java))
                    finish()
                }
            }
        }, 2000)
    }
}