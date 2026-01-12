package com.example.strivo

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.Toast
import com.example.strivo.databinding.ActivityIssuecircularBinding
import com.example.strivo.dataclass.circulardata
import com.example.strivo.dataclass.meetingdata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class Issuecircular : AppCompatActivity() {
    private lateinit var binding:ActivityIssuecircularBinding
    private lateinit var date:ImageView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityIssuecircularBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseDatabase=FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("circular")

        date=binding.issuecircdatepick
        date.setOnClickListener(){
            showDatePickerDialog()
        }

        binding.issuebtn.setOnClickListener(){
            val title=binding.circtitle.text.toString().trim()
            val date=binding.circdate.text.toString().trim()
            val desc=binding.issuecircdesc.text.toString().trim()

            if (title.isEmpty()) {
                binding.circtitle.error = "Title cannot be empty"
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }
            if (date.isEmpty()) {
                binding.circdate.error = "Date cannot be empty"
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }
            if (desc.isEmpty()) {
                binding.issuecircdesc.error = "Description cannot be empty"
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }

            savecircular(title,date,desc)
        }

        binding.issuebck.setOnClickListener(){
            onBackPressed()
        }
    }
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.UserDialog, // Apply custom style here
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val formattedDate = DateFormat.format("dd/MM/yyyy", selectedDate).toString()
                binding.circdate.setText(formattedDate)
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun savecircular(title: String,date: String,desc: String){
        databaseReference.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val id=databaseReference.push().key
                if (id == null) {
                    Toast.makeText(this@Issuecircular, "Failed to generate ID.", Toast.LENGTH_LONG).show()
                    return
                }
                val circular= circulardata(id,title,date,desc)
                databaseReference.child(id).setValue(circular)
                    .addOnSuccessListener {
                        Toast.makeText(this@Issuecircular, "Circular has been issued", Toast.LENGTH_LONG).show()
                        onBackPressed()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@Issuecircular, "Failed to issue circular: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Issuecircular,"Database Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        } )
    }
}