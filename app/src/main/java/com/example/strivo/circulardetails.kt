package com.example.strivo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.strivo.databinding.ActivityCirculardetailsBinding
import com.example.strivo.dataclass.circulardata
import com.example.strivo.dataclass.eventdata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class circulardetails : AppCompatActivity() {
    private lateinit var binding:ActivityCirculardetailsBinding
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCirculardetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id=intent.getStringExtra("circid")
        databaseReference = FirebaseDatabase.getInstance().getReference("circular")
        id?.let { retrieve(it) }


        binding.imageView42.setOnClickListener(){
            onBackPressed()
        }
    }

    private fun retrieve(id: String) {
        databaseReference.orderByChild("id").equalTo(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val circulardata = userSnapshot.getValue(circulardata::class.java)
                            binding.circdisptitle.text = circulardata?.title
                            binding.cdate.text = circulardata?.date
                            binding.cdesc.text = circulardata?.description

                        }
                    } else {
                        Toast.makeText(this@circulardetails, "Circular Not Found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@circulardetails, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}