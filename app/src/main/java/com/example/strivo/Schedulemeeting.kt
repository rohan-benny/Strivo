package com.example.strivo

import android.app.DatePickerDialog
import android.icu.text.CaseMap.Title
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.example.strivo.databinding.ActivitySchedulemeetingBinding
import com.example.strivo.dataclass.meetingdata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class Schedulemeeting : AppCompatActivity() {
    private lateinit var binding: ActivitySchedulemeetingBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var datepick : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySchedulemeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseDatabase=FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("meetings")

        binding.imageView20.setOnClickListener(){
            onBackPressed()
        }

        //calendar
        datepick=binding.cal
        datepick.setOnClickListener(){
            showDatePickerDialog()
        }

        binding.schedulebtn.setOnClickListener(){
            val title=binding.scmtitled.text.toString().trim()
            val date=binding.scmdate.text.toString().trim()
            val time=binding.scmtime.text.toString().trim()
            val venue=binding.scmvenue.text.toString().trim()
            val desc=binding.scmdesc.text.toString().trim()
            if (title.isEmpty()) {
                binding.scmtitled.error = "Title cannot be empty"
                Toast.makeText(this, "Please enter a title for the meeting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }
            if (date.isEmpty()) {
                binding.scmdate.error = "Date cannot be empty"
                Toast.makeText(this, "Please select a date for the meeting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }
            if (time.isEmpty()) {
                binding.scmtime.error = "Time cannot be empty"
                Toast.makeText(this, "Please enter a time for the meeting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }
            if (venue.isEmpty()) {
                binding.scmvenue.error = "Venue cannot be empty"
                Toast.makeText(this, "Please enter a venue for the meeting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }
            if (desc.isEmpty()) {
                binding.scmdesc.error = "Description cannot be empty"
                Toast.makeText(this, "Please enter a description for the meeting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }

            saveschedule(title,date,time,venue,desc)
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
                val formattedDate = DateFormat.format("dd MMMM yyyy", selectedDate).toString()
                binding.scmdate.setText(formattedDate)
                binding.scmdate.error = null
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun saveschedule(title: String,date: String,time: String,venue: String,desc: String){
        databaseReference.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val id=databaseReference.push().key
                    if (id == null) {
                        Toast.makeText(this@Schedulemeeting, "Failed to generate meeting ID.", Toast.LENGTH_LONG).show()
                        return
                    }
                    val meeting=meetingdata(id,title,date,time,venue,desc)
                    databaseReference.child(id).setValue(meeting)
                        .addOnSuccessListener {
                            Toast.makeText(this@Schedulemeeting,"Meeting Scheduled Successfully",Toast.LENGTH_LONG).show()
                            onBackPressed()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@Schedulemeeting,"Failed to schedule meeting: ${e.message}",Toast.LENGTH_LONG).show()
                            Log.e("ScheduleMeeting", "Firebase setValue failed", e)
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Schedulemeeting,"Database Error: ${error.message}",Toast.LENGTH_LONG).show()
                }
            } )
    }
}