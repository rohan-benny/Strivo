package com.example.strivo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.strivo.databinding.ActivityManageEventsBinding

class manage_events : AppCompatActivity() {
    private lateinit var binding: ActivityManageEventsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityManageEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView32.setOnClickListener(){
            onBackPressed()
        }
        binding.addeventbtn.setOnClickListener(){
            val intent=Intent(this,Add_event::class.java)
            startActivity(intent)
        }
        binding.vieweventbtn.setOnClickListener(){
            val intent=Intent(this,Viewevent::class.java)
            startActivity(intent)
        }
    }
}