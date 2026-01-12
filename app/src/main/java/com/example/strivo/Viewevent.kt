package com.example.strivo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.strivo.databinding.ActivityVieweventBinding
import com.example.strivo.dataclass.eventdata
import com.example.strivo.dataclass.meetingdata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Viewevent : AppCompatActivity() {
    private lateinit var binding: ActivityVieweventBinding
    private val eventlist= mutableListOf<eventdata>()
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVieweventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().getReference("deptevent")

        adapter=EventAdapter(this, eventlist)
        binding.eventsrecycler.layoutManager= LinearLayoutManager(this)
        binding.eventsrecycler.adapter=adapter

        binding.imageView37.setOnClickListener(){
            onBackPressed()
        }

        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventlist.clear()
                for(dataSnapshot in snapshot.children){
                    val fac=dataSnapshot.getValue(eventdata::class.java)
                    fac?.let{eventlist.add(it)}
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Viewevent,"Failed To retrieve data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}