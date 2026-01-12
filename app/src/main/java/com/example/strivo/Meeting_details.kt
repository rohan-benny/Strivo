package com.example.strivo

import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.provider.CalendarContract
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.strivo.databinding.ActivityClasslistBinding
import com.example.strivo.databinding.ActivityMeetingDetailsBinding
import com.example.strivo.dataclass.meetingdata
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class Meeting_details : AppCompatActivity() {
    private lateinit var binding: ActivityMeetingDetailsBinding
    private lateinit var databaseReference: DatabaseReference
    private var currentMeetingData: meetingdata? = null
    private val CALENDAR_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView14.setOnClickListener(){
            onBackPressed()
        }

        val id = intent.getStringExtra("meetid")
        databaseReference = FirebaseDatabase.getInstance().getReference("meetings")
        id?.let { retrieve(it) }

        binding.notifybtn.setOnClickListener(){
            if (checkCalendarPermissions()) {
                currentMeetingData?.let { meetData ->
                    addEventToCalendar(meetData)
                } ?: run {
                    Toast.makeText(this, "Meeting details not loaded yet. Please wait.", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestCalendarPermissions()
            }

        }


    }
    private fun retrieve(id: String) {
        databaseReference.orderByChild("id").equalTo(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val meetdata = userSnapshot.getValue(meetingdata::class.java)
                            currentMeetingData = meetdata
                            binding.textView35.text = meetdata?.title
                            binding.mdate.text = meetdata?.date
                            binding.mtime.text = meetdata?.time
                            binding.mloc.text = meetdata?.venue
                            binding.mdesc.text = meetdata?.description
                            binding.notifybtn.isEnabled = true
                        }
                    } else {
                        Toast.makeText(this@Meeting_details, "User not found", Toast.LENGTH_SHORT).show()
                        binding.notifybtn.isEnabled = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Meeting_details, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    binding.notifybtn.isEnabled = false
                }
            })
    }
    private fun checkCalendarPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestCalendarPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
            CALENDAR_PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, try to add event now
                currentMeetingData?.let { meetData ->
                    addEventToCalendar(meetData)
                }
            } else {
                // Permissions denied
                Toast.makeText(this, "Calendar permissions are required to add events automatically.", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun addEventToCalendar(meetData: meetingdata) {
        val title = meetData.title ?: "Meeting"
        val description = meetData.description ?: "No Description"
        val location = meetData.venue ?: "Online/Unspecified"
        val dateString = meetData.date // e.g., "05 March 2025"
        val timeString = meetData.time // e.g., "10:00 PM"

        // Step 1: Find a calendar ID to insert the event into
        val calendarId = getCalendarId()
        if (calendarId == -1L) {
            Toast.makeText(this, "No writable calendar found on this device.", Toast.LENGTH_LONG).show()
            return
        }

        val formatter = SimpleDateFormat("dd MMM yyyy hh a", Locale.getDefault()) // <--- CHANGE THIS LINE!

        val eventStartTimeMillis: Long
        val eventEndTimeMillis: Long

        try {
            val dateTimeString = "$dateString $timeString" // This will combine to "05 March 2025 10:00 PM"
            val eventDate = formatter.parse(dateTimeString)
            if (eventDate == null) {
                Toast.makeText(this, "Invalid date or time format.", Toast.LENGTH_SHORT).show()
                return
            }
            val calendar = Calendar.getInstance()
            calendar.time = eventDate
            eventStartTimeMillis = calendar.timeInMillis
            // Assuming a 1-hour event duration; adjust as needed
            eventEndTimeMillis = eventStartTimeMillis + 60 * 60 * 1000 // 1 hour in milliseconds

        } catch (e: Exception) {
            Toast.makeText(this, "Error parsing meeting date/time for calendar.", Toast.LENGTH_LONG).show()
            return
        }
        if (isEventAlreadyAdded(title, eventStartTimeMillis, eventEndTimeMillis)) {
            Toast.makeText(this, "This meeting is already in your calendar.", Toast.LENGTH_LONG).show()
            return // Stop the function execution if a duplicate is found
        }

        // Step 3: Create ContentValues for the event
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.DTSTART, eventStartTimeMillis)
            put(CalendarContract.Events.DTEND, eventEndTimeMillis)
            put(CalendarContract.Events.ALL_DAY, 0) // 0 for false, 1 for true
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id) // Use device's default timezone
            put(CalendarContract.Events.HAS_ALARM, 1) // 1 for true (add reminder)
        }

        // Step 4: Insert the event
        try {
            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                val eventId = uri.lastPathSegment?.toLong()
                if (eventId != null) {
                    Toast.makeText(this, "Meeting added to calendar!", Toast.LENGTH_SHORT).show()
                    addReminderToEvent(eventId)
                } else {
                    Toast.makeText(this, "Failed to get event ID after adding.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to add meeting to calendar.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Calendar permissions not granted.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "An error occurred while adding to calendar.", Toast.LENGTH_LONG).show()
        }
    }
    private fun isEventAlreadyAdded(title: String, startTimeMillis: Long, endTimeMillis: Long): Boolean {
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )

        // Selection clause: match title, start time, and end time
        val selection = "${CalendarContract.Events.TITLE} = ? AND ${CalendarContract.Events.DTSTART} = ? AND ${CalendarContract.Events.DTEND} = ?"
        val selectionArgs = arrayOf(title, startTimeMillis.toString(), endTimeMillis.toString())

        var eventExists = false
        var cursor: android.database.Cursor? = null

        try {
            cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                eventExists = true // Found at least one matching event
            }
        } catch (e: SecurityException) {

        } catch (e: Exception) {

        } finally {
            cursor?.close()
        }
        return eventExists
    }
    private fun getCalendarId(): Long {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL // <--- ADD THIS LINE
        )

        val cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null, // No specific selection initially
            null,
            CalendarContract.Calendars._ID + " ASC" // Order by ID
        )

        var calendarId: Long = -1
        cursor?.use {
            while (it.moveToNext()) {
                val calId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                val accessLevel = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL))

                // We want a calendar that we can write to. CAL_ACCESS_OWNER is also good.
                if (accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR ||
                    accessLevel >= CalendarContract.Calendars.CAL_ACCESS_ROOT ||
                    accessLevel >= CalendarContract.Calendars.CAL_ACCESS_OWNER) {
                    calendarId = calId
                    break // Found a writable calendar, use this one
                }
            }
        }
        cursor?.close()
        return calendarId
    }
    private fun addReminderToEvent(eventId: Long) {
        val reminderValues = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, 30) // Reminder 30 minutes before
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }

        // Capture the URI returned by the insert operation
        val uri = contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)

        if (uri != null) {
            // If uri is not null, the reminder was successfully added
            Toast.makeText(this, "You will be notified 30 mins before the event!", Toast.LENGTH_LONG).show()
        } else {
            // If uri is null, there was an issue adding the reminder
            Toast.makeText(this, "Failed to set reminder.", Toast.LENGTH_SHORT).show()
        }
    }


}