package com.example.strivo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.strivo.databinding.ActivityMessaginglayoutBinding
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class messaginglayout : AppCompatActivity() {
    private lateinit var binding:ActivityMessaginglayoutBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: MessageAdapter
    private val facultylist= mutableListOf<userData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMessaginglayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().getReference("faculties")

        adapter= MessageAdapter(this, facultylist)
        binding.msgrecycle.layoutManager= LinearLayoutManager(this)
        binding.msgrecycle.adapter=adapter

        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                facultylist.clear()
                for(dataSnapshot in snapshot.children){
                    val fac=dataSnapshot.getValue(userData::class.java)
                    fac?.let{facultylist.add(it)}
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@messaginglayout,"Failed To retrieve data", Toast.LENGTH_SHORT).show()
            }
        })

        binding.msgbackarrow.setOnClickListener(){
            onBackPressed()
        }

    }
}