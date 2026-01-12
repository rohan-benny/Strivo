package com.example.strivo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.strivo.databinding.ActivityBulletinBinding
import com.example.strivo.dataclass.circulardata
import com.example.strivo.dataclass.eventdata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class bulletin : AppCompatActivity() {
    private lateinit var binding:ActivityBulletinBinding
    private val circularlist= mutableListOf<circulardata>()
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: circularadapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBulletinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView24.setOnClickListener(){
            onBackPressed()
        }

        firebaseDatabase=FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("circular")
        adapter= circularadapter(this, circularlist)
        binding.circularecycle.layoutManager= LinearLayoutManager(this)
        binding.circularecycle.adapter=adapter

        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                circularlist.clear()
                for(dataSnapshot in snapshot.children){
                    val fac=dataSnapshot.getValue(circulardata::class.java)
                    fac?.let{circularlist.add(it)}
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@bulletin,"Failed To retrieve data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}