package com.example.strivo

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.Toast
import com.example.strivo.databinding.ActivityAddEventBinding
import com.example.strivo.dataclass.eventdata
import com.example.strivo.dataclass.meetingdata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Add_event : AppCompatActivity() {
    private lateinit var binding: ActivityAddEventBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var date1:ImageView
    private lateinit var date2:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseDatabase= FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("deptevent")

        date1=binding.strtdatepick
        date1.setOnClickListener(){
            showDatePickerDialog1()
        }

        date2=binding.endatepick
        date2.setOnClickListener(){
            showDatePickerDialog2()
        }

        binding.imageView35.setOnClickListener(){
            onBackPressed()
        }

        binding.schedbutton.setOnClickListener(){
            val title=binding.eventitle.text.toString().trim()
            val stdate=binding.startdate.text.toString().trim()
            val endate=binding.enddate.text.toString().trim()
            val time=binding.eventime.text.toString().trim()
            val venue=binding.venue.text.toString().trim()
            val faccod=binding.faccod.text.toString().trim()
            val stdcod=binding.studentcord.text.toString().trim()
            val desc=binding.editText2.text.toString().trim()
            if (title.isEmpty()) {
                binding.eventitle.error = "Event Title is required"
                Toast.makeText(this, "Please enter an event title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (stdate.isEmpty()) {
                binding.startdate.error = "Start Date is required"
                Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (endate.isEmpty()) {
                binding.enddate.error = "End Date is required"
                Toast.makeText(this, "Please select an end date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (time.isEmpty()) {
                binding.eventime.error = "Time is required"
                Toast.makeText(this, "Please enter event time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (venue.isEmpty()) {
                binding.venue.error = "Venue is required"
                Toast.makeText(this, "Please enter the event venue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (faccod.isEmpty()) {
                binding.faccod.error = "Faculty Coordinator is required"
                Toast.makeText(this, "Please enter faculty coordinator details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (stdcod.isEmpty()) {
                binding.studentcord.error = "Student Coordinator is required"
                Toast.makeText(this, "Please enter student coordinator details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (desc.isEmpty()) {
                binding.editText2.error = "Description is required"
                Toast.makeText(this, "Please enter event description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            try {
                val startDateObj: Date = dateFormat.parse(stdate)!!
                val endDateObj: Date = dateFormat.parse(endate)!!

                if (endDateObj.before(startDateObj)) {
                    binding.enddate.error = "End Date cannot be before Start Date"
                    Toast.makeText(this, "End Date must be on or after Start Date", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                // This catch block handles potential parsing errors, though it should be rare
                // if dates are selected via DatePickerDialog
                Toast.makeText(this, "Error parsing dates. Please re-select dates.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                return@setOnClickListener
            }

            // Clear all errors if validation passes
            binding.eventitle.error = null
            binding.startdate.error = null
            binding.enddate.error = null
            binding.eventime.error = null
            binding.venue.error = null
            binding.faccod.error = null
            binding.studentcord.error = null
            binding.editText2.error = null

            saveevent(title,stdate,endate,time,venue,faccod,stdcod,desc)
        }
    }

    private fun saveevent(title: String, stdate: String, endate: String, time: String, venue: String, faccod: String, stdcod: String, desc: String) {
        databaseReference.orderByChild("startdate").equalTo(stdate).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val id=databaseReference.push().key
                if (id == null) {
                    Toast.makeText(this@Add_event, "Failed to generate event ID.", Toast.LENGTH_LONG).show()
                    return
                }
                val event= eventdata(id,title,stdate,endate,time,venue,faccod,stdcod,desc)
                databaseReference.child(id).setValue(event)
                    .addOnSuccessListener {
                        Toast.makeText(this@Add_event,"Event Scheduled Successfully", Toast.LENGTH_LONG).show()
                        onBackPressed()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@Add_event,"Failed to schedule event: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace() // Print stack trace for debugging
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_event,"Database Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        } )
    }

    private fun showDatePickerDialog1() {
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
                val formattedDate = DateFormat.format("dd-MM-yyyy", selectedDate).toString()
                binding.startdate.setText(formattedDate)
                binding.startdate.error = null
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }
    private fun showDatePickerDialog2() {
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
                val formattedDate = DateFormat.format("dd-MM-yyyy", selectedDate).toString()
                binding.enddate.setText(formattedDate)
                binding.enddate.error = null
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }
}