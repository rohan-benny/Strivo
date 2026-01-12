package com.example.strivo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.strivo.databinding.ActivityMymeetingsBinding
import com.example.strivo.dataclass.meetingdata
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.time.measureTime

class Mymeetings : AppCompatActivity() {
    private lateinit var binding: ActivityMymeetingsBinding
    private val meetinglist= mutableListOf<meetingdata>()
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: mymeetinglistadapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMymeetingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().getReference("meetings")

        binding.imageView12.setOnClickListener(){
            onBackPressed()
        }
       // val date= listOf("05 March 2025","04 April 2025","12 April 2025","12 April 2025","12 April 2025")
       // val title= listOf("Review Meeting","Dept Meeting","Dept Meeting","Dept Meeting","Dept Meeting")

        adapter=mymeetinglistadapter(this,meetinglist)
        binding.mymeetingrecycler.layoutManager=LinearLayoutManager(this)
        binding.mymeetingrecycler.adapter=adapter

        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                meetinglist.clear()
                for(dataSnapshot in snapshot.children){
                    val fac=dataSnapshot.getValue(meetingdata::class.java)
                    fac?.let{meetinglist.add(it)}
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Mymeetings,"Failed To retrieve data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}