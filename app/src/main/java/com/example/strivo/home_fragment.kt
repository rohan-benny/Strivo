package com.example.strivo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.strivo.databinding.FragmentHomeFragmentBinding
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.strivo.dataclass.TimetableEntry // Import your TimetableEntry data class
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.os.Handler
import android.os.Looper

class home_fragment : Fragment() {
    private lateinit var binding: FragmentHomeFragmentBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference // This will point to "faculties"

    // --- Timetable Display Variables and Logic ---

    // Define your time slot ranges. Key: Firebase time slot string. Value: Pair of (start Calendar, end Calendar)
    // Based on your previous Firebase timetable structure, these time keys should match.
    private val timeSlotRanges = listOf(
        "9:00 AM" to (Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } to Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 50); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }),
        "10:00 AM" to (Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } to Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 50); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }),
        "11:00 AM" to (Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 11); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } to Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 11); set(Calendar.MINUTE, 50); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }),
        "12:00 PM" to (Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } to Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 50); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }),
        "1:40 PM" to (Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 13); set(Calendar.MINUTE, 40); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } to Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 14); set(Calendar.MINUTE, 30); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }),
        "2:40 PM" to (Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 14); set(Calendar.MINUTE, 40); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } to Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 15); set(Calendar.MINUTE, 30); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }),
        "3:40 PM" to (Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 15); set(Calendar.MINUTE, 40); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } to Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 16); set(Calendar.MINUTE, 30); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) })
    )

    // Map Firebase keys to user-friendly display strings for the time (e.g., "9:00 AM" to "9:00-9:50 AM")
    private val timeSlotDisplayMap = mapOf(
        "9:00 AM" to "9:00-9:50 AM",
        "10:00 AM" to "10:00-10:50 AM",
        "11:00 AM" to "11:00-11:50 AM",
        "12:00 PM" to "12:00-12:50 PM",
        "1:40 PM" to "1:40-2:30 PM",
        "2:40 PM" to "2:40-3:30 PM",
        "3:40 PM" to "3:40-4:30 PM"
    )

    // Map Calendar day constants to your Firebase day string keys (case-sensitive)
    // Your provided Firebase structure for timetable shows "Friday" as a day key.
    private val dayMap = mapOf(
        Calendar.MONDAY to "Monday",
        Calendar.TUESDAY to "Tuesday",
        Calendar.WEDNESDAY to "Wednesday",
        Calendar.THURSDAY to "Thursday",
        Calendar.FRIDAY to "Friday",
        Calendar.SATURDAY to "Saturday",
        Calendar.SUNDAY to "Sunday"
    )

    // Handler and Runnable for periodic updates of the timetable display
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimetableRunnable = object : Runnable {
        override fun run() {
            displayCurrentClass() // Call the function to update the display
            handler.postDelayed(this, 60 * 1000L) // Schedule to run again after 1 minute (60,000 milliseconds)
        }
    }
    // --- End Timetable Display Variables and Logic ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeFragmentBinding.inflate(inflater, container, false)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("faculties") // Database reference is already correct for faculties

        val sharedPrefs = activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val email = sharedPrefs?.getString("loginuser", "")
        email?.let { retrieve(it) }

        // --- Initialize Current Class Display ---
        displayCurrentClass() // Call this to set initial timetable display
        // --- End Initialize Current Class Display ---

        binding.meetbtn.setOnClickListener() {
            val intent = Intent(requireContext(), Mymeetings::class.java)
            requireContext().startActivity(intent)
        }
        binding.schedulebtn.setOnClickListener() {
            val intent = Intent(requireContext(), Schedulemeeting::class.java)
            requireContext().startActivity(intent)
        }
        binding.timetablebtn.setOnClickListener() {
            val intent = Intent(requireContext(), ViewMyTimetable::class.java)
            requireContext().startActivity(intent)
        }
        binding.button7.setOnClickListener() { // This is likely "Issue Circular" based on your previous HOD fragment
            val intent = Intent(requireContext(), Issuecircular::class.java)
            requireContext().startActivity(intent)
        }

        binding.eventbtn.setOnClickListener() {
            val intent = Intent(requireContext(), Viewevent::class.java) // Assuming Viewevent for faculty
            requireContext().startActivity(intent)
        }

        binding.bulletinbtn.setOnClickListener() {
            val intent = Intent(requireContext(), bulletin::class.java)
            requireContext().startActivity(intent)
        }
        binding.querybtn.setOnClickListener() {
            val intent = Intent(requireContext(), messaginglayout::class.java)
            requireContext().startActivity(intent)
        }
        binding.calendarbtn.setOnClickListener() {
            val intent = Intent(requireContext(), com.example.strivo.Calendar::class.java)
            requireContext().startActivity(intent)
        }
        binding.button3.setOnClickListener() { // This is "Rooms"
            val intent = Intent(requireContext(), Roomview::class.java)
            requireContext().startActivity(intent)
        }


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Start the periodic timetable update when the fragment becomes active/visible
        handler.post(updateTimetableRunnable)
    }

    override fun onStop() {
        super.onStop()
        // Stop the periodic timetable update when the fragment is no longer visible
        handler.removeCallbacks(updateTimetableRunnable)
    }


    private fun retrieve(email: String) {
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userdata = userSnapshot.getValue(userData::class.java)
                            val name = userdata?.name
                            val photoBase64 = userdata?.photoBase64

                            // Your existing logic for setting name
                            binding.hdname.text = name

                            // Save the unique Firebase key (ID) of the faculty node
                            val loginId = userSnapshot.key
                            if (loginId != null) {
                                activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)?.edit()?.apply {
                                    putString("loginId", loginId)
                                    apply()
                                }
                                Log.d("HomeFragment", "Login ID saved: $loginId")
                            } else {
                                Log.w("HomeFragment", "Faculty ID (userSnapshot.key) is null!")
                            }

                            if (!photoBase64.isNullOrEmpty()) {
                                try {
                                    // If your string has a prefix like "data:image/png;base64,", remove it
                                    val base64Data = photoBase64.substringAfter("base64,", photoBase64)

                                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                                    if (bitmap != null) {
                                        binding.showpropic.setImageBitmap(bitmap)
                                    } else {
                                        Toast.makeText(requireContext(), "Bitmap is null", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(requireContext(), "Error decoding image: ${e.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("HomeFragment", "Error decoding image: ${e.message}")
                                }
                            } else {
                                // If no photo is available or it's empty
                                Log.d("HomeFragment", "No profile photo found or empty.")
                                // Optional: Set a default profile picture
                                // binding.showpropic.setImageResource(R.drawable.default_profile_pic);
                                Toast.makeText(
                                    requireContext(),
                                    "Profile picture not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            // Call displayCurrentClass here again to ensure timetable updates after faculty ID is available
                            displayCurrentClass()
                        }
                    } else {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                        Log.w("HomeFragment", "No user found for email: $email")
                        // Clear loginId if user not found
                        activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)?.edit()?.apply {
                            remove("loginId")
                            apply()
                        }
                        displayCurrentClass() // Update UI to reflect no user/timetable
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("HomeFragment", "Firebase user data fetch cancelled: ${error.message}")
                    // Also update UI to reflect error
                    binding.hdname.text = "Error"
                    binding.showpropic.setImageBitmap(null) // Clear image or set error image
                    displayCurrentClass() // Update timetable section as well
                }
            })
    }


    /**
     * Determines the current time slot key (e.g., "9:00 AM") based on the provided Calendar instance.
     * Returns the Firebase key for the active slot, or null if no slot is currently active.
     */
    private fun getCurrentTimeSlotKey(currentTime: Calendar): String? {
        for ((slotKey, timeRange) in timeSlotRanges) {
            val (startCal, endCal) = timeRange
            // Check if currentTime is strictly after startCal and before endCal
            if (currentTime.after(startCal) && currentTime.before(endCal)) {
                Log.d("TimetableLogic", "Current time ${currentTime.time} is in slot: $slotKey")
                return slotKey
            }
        }
        Log.d("TimetableLogic", "Current time ${currentTime.time} is not in any active slot.")
        return null // No current slot found
    }

    /**
     * Returns the Firebase key of the very last defined time slot (e.g., "3:40 PM").
     * Used when it's after the last class of the day.
     */
    private fun getLastTimeSlotKey(): String {
        return timeSlotRanges.last().first
    }

    /**
     * Returns the Firebase key of the very first defined time slot (e.g., "9:00 AM").
     * Used when it's before the first class of the day.
     */
    private fun getFirstTimeSlotKey(): String {
        return timeSlotRanges.first().first
    }


    /**
     * Fetches and displays the current class information in the blue box section of the UI.
     * Handles displaying "Free", "No Classes Today", or details of the last/first class
     * when outside active class hours.
     */
    private fun displayCurrentClass() {
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // Ensure you retrieve the loginId for the *faculty* here
        val id = sharedPrefs.getString("loginId", null)

        // If 'loginId' is not yet available, display a loading/placeholder state and return.
        if (id.isNullOrEmpty()) {
            binding.rectsub.text = "Login to view timetable"
            binding.rectclass.text = ""
            binding.rectroom.text = ""
            binding.rectime.text = ""
            binding.rectdate.text = ""
            Log.d("TimetableDisplay", "Login ID is null or empty, cannot display timetable.")
            return
        }

        val currentCalendar = Calendar.getInstance()
        val currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK)
        val currentDayName = dayMap[currentDayOfWeek] ?: "Unknown"

        // Update the Day and Year in the blue box
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        binding.rectdate.text = dateFormat.format(currentCalendar.time)

        val currentTimeSlotKey = getCurrentTimeSlotKey(currentCalendar)

        // Determine if the current time is after the very last class of the day.
        val lastSlotEndTimeCal = timeSlotRanges.last().second.second
        val isAfterLastClass = currentTimeSlotKey == null && currentCalendar.after(lastSlotEndTimeCal)

        // Determine which time slot key to fetch from Firebase:
        val fetchTimeSlotKey = when {
            currentTimeSlotKey != null -> currentTimeSlotKey
            isAfterLastClass -> getLastTimeSlotKey()
            else -> getFirstTimeSlotKey()
        }

        // --- Handle special day cases (e.g., Sunday or "Unknown" day) ---
        if (currentDayName == "Sunday") { // Assuming no classes on Sunday
            binding.rectsub.text = "No Classes Today"
            binding.rectclass.text = ""
            binding.rectroom.text = ""
            binding.rectime.text = "Weekend"
            Log.d("TimetableDisplay", "It's Sunday, no classes.")
            return
        }
        if (currentDayName == "Unknown") {
            binding.rectsub.text = "Invalid Day"
            binding.rectclass.text = ""
            binding.rectroom.text = ""
            binding.rectime.text = ""
            Log.e("TimetableDisplay", "Could not determine day name for Calendar.DAY_OF_WEEK: $currentDayOfWeek")
            return
        }
        // --- End special day cases ---


        // Construct the Firebase database reference to the specific timetable entry
        // This targets "faculties/{faculty_id}/timetable/{day}/{time_slot}"
        val currentTimetableRef = firebaseDatabase.reference
            .child("faculties") // Remains "faculties" as per your instruction
            .child(id)
            .child("timetable")
            .child(currentDayName)
            .child(fetchTimeSlotKey)

        Log.d("TimetableDisplay", "Attempting to fetch: faculties/$id/timetable/$currentDayName/$fetchTimeSlotKey")

        currentTimetableRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timetableEntry = snapshot.getValue(TimetableEntry::class.java)

                // Determine the time text to display in the blue box based on the current context
                val timeDisplay = when {
                    currentTimeSlotKey != null -> timeSlotDisplayMap[currentTimeSlotKey] ?: "N/A" // Active class
                    isAfterLastClass -> "Classes Ended" // After school hours
                    else -> "Classes Start Soon" // Before school hours
                }
                binding.rectime.text = timeDisplay

                // Populate the subject, class, room views
                if (timetableEntry != null && timetableEntry.subject != null && timetableEntry.subject.trim().lowercase(Locale.getDefault()) != "free") {
                    binding.rectsub.text = timetableEntry.subject
                    // Combine sem and className for the 'rectclass' TextView if both are relevant
                    val semAndClass = StringBuilder()
                    timetableEntry.sem?.let { semAndClass.append(it) }
                    timetableEntry.className?.let {
                        if (semAndClass.isNotEmpty()) semAndClass.append(" ")
                        semAndClass.append(it)
                    }
                    binding.rectclass.text = semAndClass.toString()
                    binding.rectroom.text = timetableEntry.room
                    Log.d("TimetableDisplay", "Loaded class: ${timetableEntry.subject} at ${timetableEntry.room}")
                } else {
                    binding.rectsub.text = "Free"
                    binding.rectclass.text = "-"
                    binding.rectroom.text = "-"
                    Log.d("TimetableDisplay", "Slot is Free or empty.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load current timetable: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("TimetableDisplay", "Firebase timetable fetch cancelled: ${error.message}")
                binding.rectsub.text = "Error Loading"
                binding.rectclass.text = ""
                binding.rectroom.text = ""
                binding.rectime.text = "N/A"
            }
        })
    }
}