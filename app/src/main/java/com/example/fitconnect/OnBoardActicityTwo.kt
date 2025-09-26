package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnBoardActicityTwo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_on_board_acticity_two)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        findViewById<android.widget.Button>(R.id.btnNext).setOnClickListener {
            startActivity(Intent(this, OnBoardActicityThree::class.java))
        }

        findViewById<android.widget.TextView>(R.id.tvSkip).setOnClickListener {
            startActivity(Intent(this, PatientLoginActivity::class.java))
            finish()
        }
    }
}