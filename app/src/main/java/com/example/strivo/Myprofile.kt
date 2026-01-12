package com.example.strivo

import android.content.Context
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.example.strivo.databinding.ActivityMyprofileBinding
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Myprofile : AppCompatActivity() {
    private lateinit var binding: ActivityMyprofileBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMyprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase= FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("faculties")

        val sharedPrefs=getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val email=sharedPrefs?.getString("loginuser","")
        email?.let { retrieve(it) }

        binding.imageView9.setOnClickListener(){
            onBackPressed()
        }
    }

    private fun retrieve(email: String) {
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userdata = userSnapshot.getValue(userData::class.java)
                            val name = userdata?.name
                            val photoBase64 = userdata?.photoBase64

                            binding.profilename.setText(userdata?.name)
                            binding.editTextText2.setText(userdata?.email)
                            binding.editTextText3.setText(userdata?.phoneNo)

                            if (!photoBase64.isNullOrEmpty()) {
                                try {
                                    // If your string has a prefix like "data:image/png;base64,", remove it
                                    val base64Data = photoBase64.substringAfter("base64,", photoBase64)

                                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                                    if (bitmap != null) {
                                        binding.myprofileimg.setImageBitmap(bitmap)
                                    } else {
                                        Toast.makeText(this@Myprofile, "Bitmap is null", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(this@Myprofile, "Error decoding image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else {
                                Toast.makeText(
                                    this@Myprofile,
                                    "Bitmap is not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        }
                    } else {
                        Toast.makeText(this@Myprofile, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Myprofile, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}