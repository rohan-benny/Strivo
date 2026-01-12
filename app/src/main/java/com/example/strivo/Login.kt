package com.example.strivo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.strivo.databinding.ActivityLoginBinding
import com.example.strivo.dataclass.userData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseDatabase=FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("faculties")

        binding.password.setText("kjc@123")

        binding.forgotpsswd.setOnClickListener(){
            val intent=Intent(this,changepassword::class.java)
            startActivity(intent)
        }
        firebaseAuth = FirebaseAuth.getInstance()

        binding.signupbtn.setOnClickListener {
            val email = binding.username.text.toString()
            val password = binding.password.text.toString()
            if(email.isNullOrEmpty()||password.isNullOrEmpty()){
                Toast.makeText(this, "Please enter your login credentials", Toast.LENGTH_SHORT).show()
            }else{
                showLoading()
                signInWithEmailAndPassword(email,password)
            }
        }

        // password icon toggle
        binding.password.isFocusableInTouchMode = true // Ensure EditText is focusable for click detection
        binding.passclosedeye.setOnClickListener {
            togglePasswordVisibility()
        }
    }
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Function to toggle password visibility
    private fun togglePasswordVisibility() {
        val isPasswordVisible = !(binding.password.transformationMethod is android.text.method.PasswordTransformationMethod)
        if (isPasswordVisible) {
            binding.password.transformationMethod = android.text.method.PasswordTransformationMethod()
            binding.passclosedeye.setImageResource(R.drawable.closedeye) // Switch to closed eye icon
        } else {
            binding.password.transformationMethod = null
            binding.passclosedeye.setImageResource(R.drawable.openeye) // Switch to open eye icon
        }
        binding.password.setSelection(binding.password.text?.length ?: 0) // Set cursor to end after transformation
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                email?.let{retrieve(it)}

            } else {
                hideLoading()
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrieve(email: String) {
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userdata = userSnapshot.getValue(userData::class.java)
                            val role = userdata?.role
                            val id = userSnapshot.key
                            val staffid = userdata?.staffId
                            val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor=sharedPref.edit()
                            editor.putString("loginuser",email)
                            editor.putString("Role",role)
                            editor.putString("loginId",id)
                            editor.putString("staffid",staffid)
                            editor.apply()
                            id?.let { Log.d("ID", it) }
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            hideLoading()
                                                    }
                    } else {
                        hideLoading()
                        Toast.makeText(this@Login, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideLoading()
                    Toast.makeText(this@Login, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.loadingTextView.visibility = View.VISIBLE
        binding.signupbtn.isEnabled = false // Disable login button
        binding.username.isEnabled = false // Disable username field
        binding.password.isEnabled = false // Disable password field
        binding.forgotpsswd.isEnabled = false // Disable forgot password link
        binding.passclosedeye.isEnabled = false // Disable password toggle
        // You might want to also disable other interactive elements if present
    }

    // Helper function to hide the loading indicator and re-enable interaction
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.loadingTextView.visibility = View.GONE
        binding.signupbtn.isEnabled = true // Re-enable login button
        binding.username.isEnabled = true // Re-enable username field
        binding.password.isEnabled = true // Re-enable password field
        binding.forgotpsswd.isEnabled = true // Re-enable forgot password link
        binding.passclosedeye.isEnabled = true // Re-enable password toggle
    }
}