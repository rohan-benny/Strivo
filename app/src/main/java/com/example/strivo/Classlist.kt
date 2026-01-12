package com.example.strivo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.strivo.databinding.ActivityClasslistBinding
import com.example.strivo.dataclass.classlistdata
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class Classlist : AppCompatActivity() {
    private lateinit var binding: ActivityClasslistBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private val classList = mutableListOf<classlistdata>()
    private val originalClassList = mutableListOf<classlistdata>()
    private lateinit var adapter: classlistadapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClasslistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val year = intent.getStringExtra("year")
        val className = intent.getStringExtra("class")
        if (year.isNullOrEmpty() || className.isNullOrEmpty()) {
            Toast.makeText(this, "Missing class or year info", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.classname.text = className

        // Initialize Firebase reference
        databaseReference =
            FirebaseDatabase.getInstance().getReference("student").child(year).child(className)

        binding.imageView5.setOnClickListener() {
            onBackPressed()
        }

        adapter = classlistadapter(this@Classlist, classList)
        binding.classlist.layoutManager = LinearLayoutManager(this)
        binding.classlist.adapter = adapter

        binding.searchbar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the list as text changes
                filterList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed for this implementation
            }
        })


        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalClassList.clear()
                classList.clear()

                var tutorId: String? = null

                for (dataSnapshot in snapshot.children) {
                    if (dataSnapshot.key == "tutorId") {
                        tutorId = dataSnapshot.getValue(String::class.java)
                        continue // Skip tutorId from being treated as a student
                    }

                    val student = dataSnapshot.getValue(classlistdata::class.java)
                    student?.let { originalClassList.add(it) }
                }
                originalClassList.sortBy { it.name }
                classList.addAll(originalClassList)

                // Set class strength
                binding.classtrength.text = originalClassList.size.toString()
                adapter.notifyDataSetChanged()

                // Fetch and display tutor name
                if (!tutorId.isNullOrEmpty()) {
                    val tutorRef = FirebaseDatabase.getInstance()
                        .getReference("faculties")
                        .child(tutorId)

                    tutorRef.get().addOnSuccessListener { tutorSnapshot ->
                        val faculty = tutorSnapshot.getValue(userData::class.java)
                        if (faculty != null) {
                            binding.animatorname.text = faculty.name ?: "No name"
                            // You can also use:
                            // binding.email.text = faculty.email ?: "No email"
                            // binding.phone.text = faculty.phone ?: "No phone"
                        } else {
                            binding.animatorname.text = "No tutor assigned"
                        }
                    }.addOnFailureListener {
                        binding.animatorname.text = "Failed to fetch tutor"
                    }
                } else {
                    binding.animatorname.text = "No tutor assigned"
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Classlist, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalClassList // If query is empty, show the full original list
        } else {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            originalClassList.filter { student ->
                // Check if student name contains the query (case-insensitive)
                val nameMatches =
                    student.name?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery) == true
                // Check if student roll number contains the query (case-insensitive)
                // IMPORTANT: Replace 'student.rollNo' with the actual property name for roll number in classlistdata
                // If you don't have a roll number, remove this part.
                val rollNoMatches = student.studentId?.toLowerCase(Locale.getDefault())
                    ?.contains(lowerCaseQuery) == true

                nameMatches || rollNoMatches
            }.toMutableList() // Convert to mutable list for the adapter
        }

        classList.clear() // Clear the currently displayed list
        classList.addAll(filteredList) // Add filtered results
        adapter.notifyDataSetChanged() // Notify adapter to refresh RecyclerView

        // Update strength display for filtered list (optional, but good for UX)
    }
}