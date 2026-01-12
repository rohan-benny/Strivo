package com.example.strivo



import android.content.Context

import android.graphics.BitmapFactory

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle

import android.util.Base64

import android.widget.Toast

import androidx.fragment.app.Fragment

import com.example.strivo.databinding.ActivityMainBinding

import com.example.strivo.dataclass.userData

import com.google.firebase.database.DataSnapshot

import com.google.firebase.database.DatabaseError

import com.google.firebase.database.DatabaseReference

import com.google.firebase.database.FirebaseDatabase

import com.google.firebase.database.ValueEventListener



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var databaseReference: DatabaseReference

    private var position: String? = null

    private var isRoleLoaded = false // Flag to check if role is loaded



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)



        firebaseDatabase=FirebaseDatabase.getInstance()

        databaseReference=firebaseDatabase.reference.child("faculties")

        val sharedPrefs=getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val email=sharedPrefs?.getString("loginuser","")

        val position=sharedPrefs?.getString("Role",null)



        if (position == null && !email.isNullOrEmpty()) {

            retrieve(email)

        } else {

            isRoleLoaded = true

// Load appropriate home fragment based on stored role

            if (position == "HOD") {

                replaceFragment(home_fragment())

            } else {

                replaceFragment(staff_home_fragment())

            }

        }



        binding.bottomNavigationView.setOnItemSelectedListener {

            if (!isRoleLoaded) {

                Toast.makeText(this, "Please wait... loading user data", Toast.LENGTH_SHORT).show()

                return@setOnItemSelectedListener true

            }

            when (it.itemId) {

                R.id.home -> {

                    if (position == "HOD") {

                        replaceFragment(home_fragment())

                    } else {

                        replaceFragment(staff_home_fragment())

                    }

                }



                R.id.student -> replaceFragment(student_fragment())

                R.id.faculty -> replaceFragment(faculty_fragment())

                R.id.more -> replaceFragment(more_fragment())



                else -> {



                }

            }

            true

        }

    }



    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager

        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.navhost, fragment)

        fragmentTransaction.commit()

    }

    override fun onBackPressed() {

        super.onBackPressed()

        finishAffinity()

    }



    private fun retrieve(email: String) {

        databaseReference.orderByChild("email").equalTo(email)

            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()) {

                        for (userSnapshot in dataSnapshot.children) {

                            val userdata = userSnapshot.getValue(userData::class.java)

                            val position = userdata?.role

                            isRoleLoaded = true

                            getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()

                                .putString("userRole", position).apply()



                            if (position == "HOD") {

                                replaceFragment(home_fragment())

                            } else {

                                replaceFragment(staff_home_fragment())

                            }

                            break

                        }

                    } else {

                        Toast.makeText(this@MainActivity, "User not found", Toast.LENGTH_SHORT).show()

                    }

                }



                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(this@MainActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()

                }

            })

    }

}