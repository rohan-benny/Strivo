package com.example.strivo

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.strivo.databinding.ActivityViewMyTimetableBinding
import com.example.strivo.dataclass.TimetableEntry
import com.example.strivo.dataclass.circulardata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewMyTimetable : AppCompatActivity() {
    private lateinit var binding:ActivityViewMyTimetableBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private val timeSlots = listOf(
        "9:00 AM", "10:00 AM", "11:00 AM",
        "12:00 AM", "1:40 AM", "2:40 AM", "3:40 AM"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewMyTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadTimetableForDay("Monday")
        // Subject TextViews
        val subjectViews = listOf(
            binding.firstsub,
            binding.secondsub,
            binding.thirdsub,
            binding.fourthsub,
            binding.fifthsub,
            binding.sixthsub,
            binding.seventhsub
        )

        // Class TextViews
        val classViews = listOf(
            binding.firstroom,
            binding.secondroom,
            binding.thirdroom,
            binding.fourthroom,
            binding.fifthroom,
            binding.sixthroom,
            binding.seventhroom
        )

        val buttons = listOf(
            binding.btnMonday,
            binding.btnTuesday,
            binding.btnWednesday,
            binding.btnThursday,
            binding.btnFriday,
            binding.btnSaturday
        )
        buttons.forEach { it.isSelected = false }
        binding.btnMonday.isSelected = true

        binding.firsthour.setOnLongClickListener {
            val selectedDay = buttons.find { it.isSelected }?.text.toString()
            showTimetableDialog(selectedDay, "9:00 AM")
            true
        }
        binding.secondhr.setOnLongClickListener {
            val selectedDay = buttons.find { it.isSelected }?.text.toString()
            showTimetableDialog(selectedDay, "10:00 AM")
            true
        }
        binding.thirdhr.setOnLongClickListener {
            val selectedDay = buttons.find { it.isSelected }?.text.toString()
            showTimetableDialog(selectedDay, "11:00 AM")
            true
        }
        binding.fourthhr.setOnLongClickListener {
            val selectedDay = buttons.find { it.isSelected }?.text.toString()
            showTimetableDialog(selectedDay, "12:00 AM")
            true
        }
        binding.fifthhr.setOnLongClickListener {
            val selectedDay = buttons.find { it.isSelected }?.text.toString()
            showTimetableDialog(selectedDay, "1:40 AM")
            true
        }
        binding.sixthhr.setOnLongClickListener {
            val selectedDay = buttons.find { it.isSelected }?.text.toString()
            showTimetableDialog(selectedDay, "2:40 AM")
            true
        }
        binding.seventhhr.setOnLongClickListener {
            val selectedDay = buttons.find { it.isSelected }?.text.toString()
            showTimetableDialog(selectedDay, "3:40 AM")
            true
        }

        for (button in buttons) {
            button.setOnClickListener {
                for (btn in buttons) {
                    btn.isSelected = false
                }
                button.isSelected = true
                val selectedDay = button.text.toString()
                loadTimetableForDay(selectedDay)
            }

        }

        binding.imageView22.setOnClickListener(){
            onBackPressed()
        }
    }

    private fun loadTimetableForDay(day: String) {
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val id = sharedPrefs.getString("loginId", null)
        if (id.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in. Cannot load timetable.", Toast.LENGTH_SHORT).show()
            Log.w("ViewMyTimetable", "Login ID is null or empty. Cannot load timetable.")
            clearAllTimetableDisplayFields() // Clear UI if no user ID
            return
        }
        val ref = FirebaseDatabase.getInstance().reference
            .child("faculties")
            .child(id)
            .child("timetable")
            .child(day)

        val subjectViews = listOf(
            binding.firstsub,
            binding.secondsub,
            binding.thirdsub,
            binding.fourthsub,
            binding.fifthsub,
            binding.sixthsub,
            binding.seventhsub
        )

        val classViews = listOf(
            binding.firstroom,
            binding.secondroom,
            binding.thirdroom,
            binding.fourthroom,
            binding.fifthroom,
            binding.sixthroom,
            binding.seventhroom
        )

        val classNameViews = listOf(
            binding.firstclassname,
            binding.secondclassname,
            binding.thirdclassname,
            binding.fourthclassname,
            binding.fifthclassname,
            binding.sixthclassname,
            binding.seventhclassname
        )

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for ((index, time) in timeSlots.withIndex()) {
                    val slotData = snapshot.child(time).getValue(TimetableEntry::class.java)

                    if (slotData != null && !slotData.subject.isNullOrBlank()) { // Simplified check for 'Free' state
                        subjectViews[index].text = slotData.subject
                        classViews[index].text = slotData.room ?: "-" // Default to "-" if room is null
                        val semClassText = StringBuilder()
                        slotData.sem?.let { semClassText.append(it) }
                        slotData.className?.let {
                            if (semClassText.isNotEmpty()) semClassText.append(" ")
                            semClassText.append(it)
                        }
                        classNameViews[index].text = if (semClassText.isEmpty()) "-" else semClassText.toString()
                    } else {
                        subjectViews[index].text = "Free"
                        classViews[index].text = "-"
                        classNameViews[index].text = "-"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewMyTimetable, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                clearAllTimetableDisplayFields()
            }
        })
    }
    private fun clearAllTimetableDisplayFields() {
        val subjectViews = listOf(binding.firstsub, binding.secondsub, binding.thirdsub, binding.fourthsub, binding.fifthsub, binding.sixthsub, binding.seventhsub)
        val classViews = listOf(binding.firstroom, binding.secondroom, binding.thirdroom, binding.fourthroom, binding.fifthroom, binding.sixthroom, binding.seventhroom)
        val classNameViews = listOf(binding.firstclassname, binding.secondclassname, binding.thirdclassname, binding.fourthclassname, binding.fifthclassname, binding.sixthclassname, binding.seventhclassname)

        for (i in 0 until timeSlots.size) {
            if (i < subjectViews.size) subjectViews[i].text = "Free"
            if (i < classViews.size) classViews[i].text = "-"
            if (i < classNameViews.size) classNameViews[i].text = "-"
        }
    }




    fun showTimetableDialog(day: String, time: String) {
        val sharedPrefs=getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val id=sharedPrefs?.getString("loginId","")
        if (id.isNullOrEmpty()) { // Explicitly check if ID is null or empty
            Toast.makeText(this, "User not logged in. Cannot edit timetable.", Toast.LENGTH_SHORT).show()
            Log.w("ViewMyTimetable", "Login ID is null or empty. Cannot open timetable dialog.")
            return
        }
        firebaseDatabase= FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("faculties").child(id!!).child("timetable").child(day).child(time)
        val dialogView = layoutInflater.inflate(R.layout.timetablepopup, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val dayTextView = dialogView.findViewById<TextView>(R.id.dayname)
        val timeTextView = dialogView.findViewById<TextView>(R.id.tabletime)
        val subjectEditText = dialogView.findViewById<EditText>(R.id.subject)
        val dropdownyear = dialogView.findViewById<AutoCompleteTextView>(R.id.autocompletetimeyear)
        val dropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.autocompletetime)
        val roomEditText = dialogView.findViewById<EditText>(R.id.editTextText4)
        val saveButton = dialogView.findViewById<Button>(R.id.tsavebtn)
        val cancelButton = dialogView.findViewById<Button>(R.id.tcancelbtn)

        // Set values
        dayTextView.text = day
        timeTextView.text = time
        val items1 = listOf("MCA A","MCA B","MCA C","MCA D","MCS CS","MCS DS")
        val adapter1 = ArrayAdapter(dialog.context, R.layout.dropdown_item, items1)
        dropdown.setAdapter(adapter1)
        val semitems = listOf("I","II","III","IV")
        val semadapter = ArrayAdapter(dialog.context, R.layout.dropdown_item, semitems)
        dropdownyear.setAdapter(semadapter)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val existingEntry = snapshot.getValue(TimetableEntry::class.java)
                if (existingEntry != null) {
                    subjectEditText.setText(existingEntry.subject)
                    dropdown.setText(existingEntry.className, false) // false to not show dropdown on initial set
                    dropdownyear.setText(existingEntry.sem, false)
                    roomEditText.setText(existingEntry.room)
                }
                dialog.show() // Show the dialog only after data is populated or if no data exists
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewMyTimetable, "Failed to load existing data: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewMyTimetable", "Error loading timetable entry: ${error.message}")
                dialog.show() // Still show the dialog, but fields will be empty
            }
        })

        // Button logic
        saveButton.setOnClickListener {
            val subject = subjectEditText.text.toString().trim()
            val className = dropdown.text.toString().trim()
            val sem=dropdownyear.text.toString().trim()
            val room = roomEditText.text.toString().trim()

            if (subject.isEmpty()) {
                subjectEditText.error = "Subject cannot be empty"
                Toast.makeText(this@ViewMyTimetable, "Please enter a subject", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop execution if validation fails
            }
            if (className.isEmpty()) {
                dropdown.error = "Class Name cannot be empty"
                Toast.makeText(this@ViewMyTimetable, "Please select a class name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (sem.isEmpty()) {
                dropdownyear.error = "Semester cannot be empty"
                Toast.makeText(this@ViewMyTimetable, "Please select a semester", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (room.isEmpty()) {
                roomEditText.error = "Room cannot be empty"
                Toast.makeText(this@ViewMyTimetable, "Please enter a room", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            subjectEditText.error = null
            dropdown.error = null
            dropdownyear.error = null
            roomEditText.error = null

            saveTimeTable(subject,className,sem,room,day,dialog)

            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveTimeTable(subject: String, className: String, sem: String, room: String,day:String ,dialog:AlertDialog) {
        databaseReference.addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timetable= TimetableEntry(subject,className,sem,room)
                databaseReference.setValue(timetable)
                    .addOnSuccessListener {
                        loadTimetableForDay(day)
                        Toast.makeText(this@ViewMyTimetable,"TimeTable Updated", Toast.LENGTH_LONG).show()
                        dialog.dismiss() // Dismiss dialog on success
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@ViewMyTimetable,"Failed to update timetable: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("ViewMyTimetable", "Firebase setValue failed", e)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewMyTimetable,"Database Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        } )
    }

}