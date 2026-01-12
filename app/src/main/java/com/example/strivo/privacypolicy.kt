package com.example.strivo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.strivo.databinding.ActivityPrivacypolicyBinding

class privacypolicy : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacypolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPrivacypolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.privacybackarrow.setOnClickListener(){
            onBackPressed()
        }
    }
}