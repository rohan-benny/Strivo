package com.example.strivo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.example.strivo.databinding.ActivityChangepasswordBinding
import com.google.firebase.auth.FirebaseAuth

class changepassword : AppCompatActivity() {
    private lateinit var binding: ActivityChangepasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityChangepasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val email = intent.getStringExtra("email").toString()
        binding.emailtextbx.setText(email)

        binding.sendbtn.setOnClickListener(){
            //val email = intent.getStringExtra("email").toString()
            // Send password reset email to the provided email address
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Email sent successfully
                        Toast.makeText(
                            this@changepassword,
                            "Password reset email sent to $email",
                            Toast.LENGTH_SHORT
                        ).show()
                        Handler().postDelayed({
                            onBackPressed()
                            finish()
                        },3000)
                    } else {
                        // Error occurred while sending email
                        Toast.makeText(
                            this,
                            "Failed to send password reset email. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        binding.imageView10.setOnClickListener(){
            onBackPressed()
        }
    }
}