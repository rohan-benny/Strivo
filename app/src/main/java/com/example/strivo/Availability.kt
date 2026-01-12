package com.example.strivo

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View // Make sure View is imported for visibility properties
import android.widget.Toast
import com.example.strivo.databinding.ActivityAvailabilityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Availability : AppCompatActivity() {

    private lateinit var binding: ActivityAvailabilityBinding
    private lateinit var databaseReference: DatabaseReference

    private var selectedRoomId: String? = null
    private val calendar = Calendar.getInstance()

    private val timeSlotDisplayMap = mapOf(
        "8_9" to "9:00-9:50 AM",
        "9_10" to "10:00-10:50 AM",
        "10_11" to "11:00-11:50 AM",
        "11_12" to "12:00-12:50 PM",
        "1_2" to "1:40-2:30 PM",
        "2_3" to "2:40-3:30 PM",
        "3_4" to "3:40-4:30 PM"
    )

    private val allFirebaseTimeSlotsInOrder = listOf(
        "8_9", "9_10", "10_11", "11_12", "1_2", "2_3", "3_4"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailabilityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Change View.GONE to View.INVISIBLE here
        binding.timeSlotsContainer.visibility = View.INVISIBLE // KEEP THE SPACE

        // Retrieve room details passed from Roomview
        selectedRoomId = intent.getStringExtra("roomId")
        val roomName = intent.getStringExtra("roomName")
        val roomType = intent.getStringExtra("roomType")
        val roomCapacity = intent.getStringExtra("roomCapacity")

        // Populate room details in the top card
        binding.avroomname.text = roomName
        binding.avroomcat.text = roomType?.capitalizeWords()
        binding.avroomcapacity.text = roomCapacity

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        // Set up back button
        binding.availablebackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set initial date to today and display it
        updateDateInView(calendar)
        // fetchAndDisplayAvailability(calendar) // Still keep this commented out

        // Set up Date Picker clicks
        binding.editTextText.setOnClickListener {
            showDatePickerDialog()
        }
        binding.avdatepick.setOnClickListener {
            showDatePickerDialog()
        }

        // Set up Search Button click listener
        binding.avsearchbtn.setOnClickListener {
            // Change View.GONE to View.INVISIBLE here as well
            binding.timeSlotsContainer.visibility = View.INVISIBLE // KEEP THE SPACE during refresh
            fetchAndDisplayAvailability(calendar)
        }

        // Initialize the display time for each slot TextView based on the mapping
        setupTimeSlotTextViews()
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.UserDialog,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                updateDateInView(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun updateDateInView(selectedCalendar: Calendar) {
        val fullDateFormat = SimpleDateFormat("dd MMMM EEEE", Locale.getDefault())
        binding.textView113.text = fullDateFormat.format(selectedCalendar.time)

        val editTextDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.editTextText.setText(editTextDateFormat.format(selectedCalendar.time))
    }

    private fun fetchAndDisplayAvailability(selectedCalendar: Calendar) {
        val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
        val dayOfWeekFirebase = SimpleDateFormat("EEE", Locale.getDefault()).format(selectedCalendar.time).toLowerCase(Locale.getDefault())

        resetTimeSlotButtons()

        if (selectedRoomId != null) {
            val roomId = selectedRoomId!!

            Log.d("Availability", "Fetching for Room ID: $roomId, Date: $selectedDate, Day: $dayOfWeekFirebase")

            val defaultScheduleRef = databaseReference.child("rooms").child(roomId).child("schedule").child(dayOfWeekFirebase)
            defaultScheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(defaultScheduleSnapshot: DataSnapshot) {
                    val defaultAvailableSlots = mutableSetOf<String>()
                    if (defaultScheduleSnapshot.exists()) {
                        for (slotSnapshot in defaultScheduleSnapshot.children) {
                            val timeSlotKey = slotSnapshot.key
                            val status = slotSnapshot.getValue(String::class.java)
                            if (status == "A" && timeSlotKey != null) {
                                defaultAvailableSlots.add(timeSlotKey)
                            }
                        }
                        Log.d("Availability", "Default available slots for $dayOfWeekFirebase: $defaultAvailableSlots")

                        val bookingsRef = databaseReference.child("bookings")
                        bookingsRef.orderByChild("date").equalTo(selectedDate)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(bookingsSnapshot: DataSnapshot) {
                                    val bookedSlotsForRoom = mutableSetOf<String>()
                                    if (bookingsSnapshot.exists()) {
                                        for (bookingSnapshot in bookingsSnapshot.children) {
                                            val bookedRoomId = bookingSnapshot.child("roomId").getValue(String::class.java)
                                            if (bookedRoomId == roomId) {
                                                for (timeSlotEntry in bookingSnapshot.child("timeSlots").children) {
                                                    val bookedTimeSlotKey = timeSlotEntry.value as? String
                                                    if (bookedTimeSlotKey != null) {
                                                        bookedSlotsForRoom.add(bookedTimeSlotKey)
                                                    }
                                                }
                                            }
                                        }
                                        Log.d("Availability", "Booked slots for $roomId on $selectedDate: $bookedSlotsForRoom")
                                    } else {
                                        Log.d("Availability", "No bookings found for $selectedDate for any room.")
                                    }

                                    updateTimeSlotButtons(defaultAvailableSlots, bookedSlotsForRoom)
                                    // Change View.GONE to View.VISIBLE (this was correct before, just ensuring it's not changed)
                                    binding.timeSlotsContainer.visibility = View.VISIBLE
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@Availability, "Failed to load bookings: ${error.message}", Toast.LENGTH_LONG).show()
                                    Log.e("Availability", "Booking data fetch cancelled: ${error.message}", error.toException())
                                    updateTimeSlotButtons(defaultAvailableSlots, emptySet())
                                    // Change View.GONE to View.VISIBLE (this was correct before)
                                    binding.timeSlotsContainer.visibility = View.VISIBLE
                                }
                            })

                    } else {
                        Toast.makeText(this@Availability, "No default schedule for this day.", Toast.LENGTH_SHORT).show()
                        // Change View.GONE to View.INVISIBLE here
                        binding.timeSlotsContainer.visibility = View.INVISIBLE // KEEP THE SPACE
                        return
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Availability, "Failed to load default schedule: ${error.message}", Toast.LENGTH_LONG).show()
                    Log.e("Availability", "Default schedule fetch cancelled: ${error.message}", error.toException())
                    updateTimeSlotButtons(emptySet(), emptySet())
                    // Change View.GONE to View.INVISIBLE here
                    binding.timeSlotsContainer.visibility = View.INVISIBLE // KEEP THE SPACE
                }
            })
        } else {
            Toast.makeText(this@Availability, "Room ID not found. Cannot fetch availability.", Toast.LENGTH_LONG).show()
            Log.e("Availability", "selectedRoomId is null. Make sure it's passed via Intent from Roomview.")
            // Change View.GONE to View.INVISIBLE here
            binding.timeSlotsContainer.visibility = View.INVISIBLE // KEEP THE SPACE
            finish()
        }
    }


    private fun setupTimeSlotTextViews() {
        val textViewMap = mapOf(
            binding.avfirsthr to "8_9",
            binding.avsecondhr to "9_10",
            binding.avthirdhr to "10_11",
            binding.avfourthhr to "11_12",
            binding.avfifthhr to "1_2",
            binding.avsixthhr to "2_3",
            binding.avseventhhr to "3_4"
        )
        textViewMap.forEach { (textView, firebaseKey) ->
            textView.text = timeSlotDisplayMap[firebaseKey] ?: "N/A"
        }
    }

    private fun resetTimeSlotButtons() {
        val redDrawable = getDrawable(R.drawable.redavbtn)
        val allButtons = listOf(
            binding.avfirstbtn, binding.avsecondbtn, binding.avthirdbtn,
            binding.avfourthbtn, binding.avfifthbtn, binding.avsixthbtn,
            binding.avseventhbtn
        )

        allButtons.forEach { button ->
            button.background = redDrawable
            button.text = "O"
            button.isEnabled = false
        }
    }

    private fun updateTimeSlotButtons(defaultAvailableSlots: Set<String>, bookedSlotsForRoom: Set<String>) {
        val redDrawable = getDrawable(R.drawable.redavbtn)
        val greenDrawable = getDrawable(R.drawable.greenavbtn)

        val buttonMap = mapOf(
            "8_9" to binding.avfirstbtn,
            "9_10" to binding.avsecondbtn,
            "10_11" to binding.avthirdbtn,
            "11_12" to binding.avfourthbtn,
            "1_2" to binding.avfifthbtn,
            "2_3" to binding.avsixthbtn,
            "3_4" to binding.avseventhbtn
        )

        allFirebaseTimeSlotsInOrder.forEach { firebaseKey ->
            val button = buttonMap[firebaseKey]
            if (button != null) {
                val isAvailable = defaultAvailableSlots.contains(firebaseKey) && !bookedSlotsForRoom.contains(firebaseKey)

                if (isAvailable) {
                    button.background = greenDrawable
                    button.text = "A"
                    button.isEnabled = true
                } else {
                    button.background = redDrawable
                    button.text = "O"
                    button.isEnabled = false
                }
            }
        }
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") {
            it.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
    }
}