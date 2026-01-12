package com.example.strivo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.strivo.databinding.ActivityRoomviewBinding // Ensure this import is correct
import com.example.strivo.dataclass.RoomData // Make sure you're importing RoomData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Roomview : AppCompatActivity() {

    private lateinit var binding: ActivityRoomviewBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var roomList: ArrayList<RoomData> // Use RoomData here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listener for the back arrow (from your XML)
        binding.roomback.setOnClickListener {
            onBackPressed()// Go back to the previous activity/fragment
        }

        binding.roomrecycler.layoutManager = LinearLayoutManager(this)
        binding.roomrecycler.setHasFixedSize(true) // For performance optimization

        // --- Initialize room list and adapter ---
        roomList = arrayListOf()
        roomAdapter = RoomAdapter(roomList) // Pass the empty list initially
        binding.roomrecycler.adapter = roomAdapter

        roomAdapter.setOnItemClickListener(object : RoomAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val selectedRoom = roomList[position]
                val intent = Intent(this@Roomview,Availability::class.java).apply {
                    putExtra("roomId", selectedRoom.id)
                    putExtra("roomName", selectedRoom.name)
                    putExtra("roomType", selectedRoom.type)
                    putExtra("roomCapacity", selectedRoom.capacity)
                    //Add any other details you want to pass, e.g., selectedRoom.schedule
                }
                startActivity(intent)
            }
        })
        databaseReference = FirebaseDatabase.getInstance().getReference("rooms") // "rooms" is your top-level node for rooms
        fetchRoomData()
    }

    private fun fetchRoomData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                roomList.clear() // Clear previous data to avoid duplicates if data is re-fetched

                if (dataSnapshot.exists()) {
                    for (roomSnapshot in dataSnapshot.children) {
                        val room = roomSnapshot.getValue(RoomData::class.java) // Deserialize into RoomData
                        if (room != null) {
                            roomList.add(room)
                        }
                    }
                    roomAdapter.notifyDataSetChanged() // Tell the adapter that its data has changed
                } else {
                    Toast.makeText(this@Roomview, "No rooms found in database.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Roomview, "Failed to load rooms: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}