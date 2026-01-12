package com.example.strivo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.strivo.databinding.ActivityEventdetailsviewBinding
import com.example.strivo.dataclass.eventdata
import com.example.strivo.dataclass.meetingdata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Eventdetailsview : AppCompatActivity() {
    private lateinit var binding:ActivityEventdetailsviewBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEventdetailsviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id=intent.getStringExtra("eventid")
        databaseReference = FirebaseDatabase.getInstance().getReference("deptevent")
        id?.let { retrieve(it) }

        binding.eventbackbtn.setOnClickListener(){
            onBackPressed()
        }
    }

    private fun retrieve(id: String) {
        databaseReference.orderByChild("id").equalTo(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val eventdata = userSnapshot.getValue(eventdata::class.java)
                            binding.vieweventname.text = eventdata?.title
                            binding.textView78.text = eventdata?.startdate
                            binding.textView79.text = eventdata?.startdate
                            binding.textView82.text = eventdata?.time
                            binding.textView83.text = eventdata?.venue
                            binding.textView87.text = eventdata?.studentcod
                            binding.textView85.text = eventdata?.facultycod
                            binding.textView89.text = eventdata?.description

                        }
                    } else {
                        Toast.makeText(this@Eventdetailsview, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Eventdetailsview, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}