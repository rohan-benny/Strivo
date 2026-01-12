package com.example.strivo

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.strivo.databinding.FragmentStaffHomeFragmentBinding
import com.example.strivo.dataclass.userData
import com.example.strivo.dataclass.TimetableEntry // Import your TimetableEntry data class
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.os.Handler
import android.os.Looper
import android.util.Log // For debugging

class staff_home_fragment : Fragment() {
    private lateinit var binding: FragmentStaffHomeFragmentBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference // Points to "faculties" node

    // --- Timetable Display Variables and Logic ---

    // Define your time slot ranges. Key: Firebase time slot string. Value: Pair of (start Calendar, end Calendar)
    // IMPORTANT: Ensure these time slot keys and their 24-hour Calendar values match your Firebase database exactly.
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
    private val dayMap = mapOf(
        Calendar.MONDAY to "Monday",
        Calendar.TUESDAY to "Tuesday",
        Calendar.WEDNESDAY to "Wednesday",
        Calendar.THURSDAY to "Thursday",
        Calendar.FRIDAY to "Friday",
        Calendar.SATURDAY to "Saturday",
        Calendar.SUNDAY to "Sunday" // Include if you want specific handling for Sunday
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
        // Any non-UI initialization that needs to happen early, but before view creation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // Changed return type from View? to View for non-null guarantee
        binding = FragmentStaffHomeFragmentBinding.inflate(inflater, container, false)
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("faculties")

        // Retrieve the logged-in user's email to fetch their profile data
        val sharedPrefs = activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val email = sharedPrefs?.getString("loginuser", "")
        email?.let { retrieve(it) } // Call retrieve to fetch user data (name, photo) and save loginId

        // --- Initialize Current Class Display ---
        // Call this once on fragment creation. It will likely show "Loading..." initially
        // and then get updated fully once 'retrieve' finishes and saves the 'loginId'.
        displayCurrentClass()
        // --- End Initialize Current Class Display ---


        // Set up click listeners for various navigation buttons
        binding.smeetbtn.setOnClickListener {
            val intent = Intent(requireContext(), Mymeetings::class.java)
            requireContext().startActivity(intent)
        }
        binding.stimetablebtn.setOnClickListener {
            val intent = Intent(requireContext(), ViewMyTimetable::class.java)
            requireContext().startActivity(intent)
        }
        binding.stfschedulebtn.setOnClickListener {
            val intent = Intent(requireContext(), Schedulemeeting::class.java)
            requireContext().startActivity(intent)
        }
        binding.stfmanagebtn.setOnClickListener {
            val intent = Intent(requireContext(), manage_events::class.java)
            requireContext().startActivity(intent)
        }
        binding.stfviewcircbtn.setOnClickListener {
            val intent = Intent(requireContext(), bulletin::class.java)
            requireContext().startActivity(intent)
        }
        binding.stfroomview.setOnClickListener {
            val intent = Intent(requireContext(), Roomview::class.java)
            requireContext().startActivity(intent)
        }
        binding.stfquerybtn.setOnClickListener {
            val intent = Intent(requireContext(), messaginglayout::class.java)
            requireContext().startActivity(intent)
        }
        binding.stfcalendarbtn.setOnClickListener {
            val intent = Intent(requireContext(), com.example.strivo.Calendar::class.java)
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

                            // Save the unique Firebase key (ID) of the faculty node
                            // This 'loginId' is crucial for fetching the specific faculty's timetable
                            val loginId = userSnapshot.key
                            if (loginId != null) {
                                activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)?.edit()?.apply {
                                    putString("loginId", loginId)
                                    apply()
                                }
                                Log.d("StaffHomeFragment", "Login ID saved: $loginId")
                            } else {
                                Log.w("StaffHomeFragment", "Faculty ID (userSnapshot.key) is null!")
                            }

                            binding.staffname.text = name

                            if (!photoBase64.isNullOrEmpty()) {
                                try {
                                    // Remove "data:image/png;base64," prefix if present, as Base64.decode expects raw data
                                    val base64Data = photoBase64.substringAfter("base64,", photoBase64)
                                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                                    if (bitmap != null) {
                                        binding.facpropic.setImageBitmap(bitmap)
                                    } else {
                                        Toast.makeText(requireContext(), "Bitmap is null after decode", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: IllegalArgumentException) {
                                    Log.e("StaffHomeFragment", "Error decoding image (Invalid Base64 string): ${e.message}")
                                    Toast.makeText(requireContext(), "Error decoding image: Invalid format", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("StaffHomeFragment", "General error decoding image: ${e.message}")
                                    Toast.makeText(requireContext(), "Error decoding image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Optionally, set a default placeholder image if no profile picture is available
                                // binding.facpropic.setImageResource(R.drawable.default_profile_picture);
                                Log.d("StaffHomeFragment", "No profile photo found or empty.")
                            }

                            // IMPORTANT: Once loginId is saved and user data is retrieved,
                            // call displayCurrentClass again to ensure timetable data is fetched
                            // using the correct faculty ID.
                            displayCurrentClass()
                        }
                    } else {
                        Toast.makeText(requireContext(), "User not found in database.", Toast.LENGTH_SHORT).show()
                        Log.w("StaffHomeFragment", "No user found for email: $email")
                        // If user not found, ensure loginId is cleared from SharedPreferences
                        activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)?.edit()?.apply {
                            remove("loginId")
                            apply()
                        }
                        displayCurrentClass() // Update UI to reflect no user/timetable
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("StaffHomeFragment", "Firebase user data fetch cancelled: ${error.message}")
                    // Also update UI to reflect error
                    binding.staffname.text = "Error"
                    binding.facpropic.setImageBitmap(null) // Clear image or set error image
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
        val id = sharedPrefs.getString("loginId", null) // Retrieve the faculty's unique ID

        // If 'loginId' is not yet available (e.g., app just started, or user not logged in),
        // display a loading/placeholder state and return. The 'retrieve' function will
        // eventually provide the ID and re-call this function.
        if (id.isNullOrEmpty()) {
            binding.sub.text = "Login to view timetable" // Corresponds to your <TextView android:id="@+id/sub">
            binding.rclass.text = "" // Corresponds to your <TextView android:id="@+id/rclass">
            binding.room.text = "" // Corresponds to your <TextView android:id="@+id/room">
            binding.time.text = "" // Corresponds to your <TextView android:id="@+id/time">
            binding.date.text = "" // Corresponds to your <TextView android:id="@+id/date">
            Log.d("TimetableDisplay", "Login ID is null or empty, cannot display timetable.")
            return
        }

        val currentCalendar = Calendar.getInstance() // Get current time and date
        val currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK) // Get day of week as Calendar constant
        val currentDayName = dayMap[currentDayOfWeek] ?: "Unknown" // Convert to string name

        // Update the Day and Year in the blue box (e.g., "Wednesday, 2025")
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()) // EEEE for full day name, MMMM for full month name, dd for day of month
        binding.date.text = dateFormat.format(currentCalendar.time) // Updated to use your 'date' ID

        val currentTimeSlotKey = getCurrentTimeSlotKey(currentCalendar)

        // Determine if the current time is after the very last class of the day.
        // This compares the current time to the *end time* of the last defined slot.
        val lastSlotEndTimeCal = timeSlotRanges.last().second.second // Get the Calendar object for the end of the last slot
        val isAfterLastClass = currentTimeSlotKey == null && currentCalendar.after(lastSlotEndTimeCal)

        // Determine which time slot key to fetch from Firebase:
        // 1. If currently in an active class slot.
        // 2. If after the last class, show the details of the last class.
        // 3. Otherwise (before the first class), show the details of the first class.
        val fetchTimeSlotKey = when {
            currentTimeSlotKey != null -> currentTimeSlotKey // Currently in an active class slot
            isAfterLastClass -> getLastTimeSlotKey()      // After last class, show last class's details
            else -> getFirstTimeSlotKey()                  // Before first class, show first class's details
        }

        // --- Handle special day cases (e.g., Sunday or "Unknown" day) ---
        if (currentDayName == "Sunday") { // Assuming no classes on Sunday
            binding.sub.text = "No Classes Today"
            binding.rclass.text = ""
            binding.room.text = ""
            binding.time.text = "Weekend" // Or "Enjoy Your Day!"
            Log.d("TimetableDisplay", "It's Sunday, no classes.")
            return
        }
        if (currentDayName == "Unknown") { // Should ideally not happen if dayMap is comprehensive
            binding.sub.text = "Invalid Day"
            binding.rclass.text = ""
            binding.room.text = ""
            binding.time.text = ""
            Log.e("TimetableDisplay", "Could not determine day name for Calendar.DAY_OF_WEEK: $currentDayOfWeek")
            return
        }
        // --- End special day cases ---


        // Construct the Firebase database reference to the specific timetable entry
        val currentTimetableRef = firebaseDatabase.reference
            .child("faculties")
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
                binding.time.text = timeDisplay // Updated to use your 'time' ID

                // Populate the subject, class, room, and semester views
                // If timetableEntry is null, or subject is "Free", show "Free" status
                if (timetableEntry != null && timetableEntry.subject != null && timetableEntry.subject.trim().lowercase(Locale.getDefault()) != "free") {
                    binding.sub.text = timetableEntry.subject // Updated to use your 'sub' ID
                    // Combine sem and className for the 'rclass' TextView if both are relevant
                    val semAndClass = StringBuilder()
                    timetableEntry.sem?.let { semAndClass.append(it) }
                    timetableEntry.className?.let {
                        if (semAndClass.isNotEmpty()) semAndClass.append(" ")
                        semAndClass.append(it)
                    }
                    binding.rclass.text = semAndClass.toString() // Updated to use your 'rclass' ID
                    binding.room.text = timetableEntry.room // Updated to use your 'room' ID
                    Log.d("TimetableDisplay", "Loaded class: ${timetableEntry.subject} at ${timetableEntry.room}")
                } else {
                    binding.sub.text = "Free"
                    binding.rclass.text = "-"
                    binding.room.text = "-"
                    Log.d("TimetableDisplay", "Slot is Free or empty.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load current timetable: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("TimetableDisplay", "Firebase timetable fetch cancelled: ${error.message}")
                binding.sub.text = "Error Loading"
                binding.rclass.text = ""
                binding.room.text = ""
                binding.time.text = "N/A"
            }
        })
    }
    // --- End New Functions for Timetable Display ---
}