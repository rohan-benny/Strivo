package com.example.strivo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.databinding.ActivityCalendarBinding // <--- IMPORTANT: Your View Binding import
import com.example.strivo.MonthAdapter // <--- IMPORTANT: Your MonthAdapter
import com.example.strivo.dataclass.CalendarEvent
import com.example.strivo.dataclass.MonthEvents
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class Calendar : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding // Declare your binding object
    private lateinit var monthAdapter: MonthAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater) // Initialize binding
        setContentView(binding.root) // Set the root view from binding

        // Initialize Firebase Realtime Database reference.
        // "events" is the root node where your event data is stored in Firebase, as per your screenshot
        database = FirebaseDatabase.getInstance().getReference("events")

        // Initialize RecyclerView for displaying months using binding
        // The ID of your RecyclerView in activity_calendar.xml must be 'monthRecyclerView'
        binding.monthRecyclerView.layoutManager = LinearLayoutManager(this)
        monthAdapter = MonthAdapter(this)
        binding.monthRecyclerView.adapter = monthAdapter

        // Set up click listener for the back button using binding
        // The ID of your back button in activity_calendar.xml must be 'calbackbtn'
        binding.calbackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Start fetching events from Firebase when the activity is created
        fetchEventsFromFirebase()
    }

    private fun fetchEventsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allEvents = mutableListOf<CalendarEvent>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(CalendarEvent::class.java)
                    event?.let {
                        allEvents.add(it)
                    }
                }
                Log.d("CalendarActivity", "Fetched ${allEvents.size} events from Firebase.")
                processEventsAndDisplay(allEvents)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CalendarActivity", "Failed to read events: ${error.message}")
            }
        })
    }

    private fun processEventsAndDisplay(events: List<CalendarEvent>) {
        val validEvents = events.filter { it.startDate != null && it.category != null }

        val sortedEvents = validEvents.sortedWith(
            compareBy<CalendarEvent> { it.year }
                .thenBy {
                    getMonthNumber(it.month!!)
                }
                .thenBy {
                    it.displayDate?.split("-")?.firstOrNull()?.toIntOrNull() ?: 0
                }
        )

        val groupedEventsMap = sortedEvents.groupBy { "${it.month} ${it.year}" }

        val monthEventsList = groupedEventsMap.map { (monthYearKey, eventsInMonth) ->
            val monthNameOnly = monthYearKey.split(" ")[0]
            MonthEvents(monthNameOnly, eventsInMonth)
        }

        monthAdapter.submitList(monthEventsList)
        Log.d("CalendarActivity", "Displayed ${monthEventsList.size} months.")
    }

    private fun getMonthNumber(monthName: String): Int {
        return when (monthName.toLowerCase(Locale.getDefault())) {
            "january" -> 1
            "february" -> 2
            "march" -> 3
            "april" -> 4
            "may" -> 5
            "june" -> 6
            "july" -> 7
            "august" -> 8
            "september" -> 9
            "october" -> 10
            "november" -> 11
            "december" -> 12
            else -> 0
        }
    }
}