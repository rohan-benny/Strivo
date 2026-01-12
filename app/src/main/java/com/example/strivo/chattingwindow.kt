package com.example.strivo

import android.content.Context
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.strivo.databinding.ActivityChattingwindowBinding
import com.example.strivo.dataclass.meetingdata
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class chattingwindow : AppCompatActivity() {
    private lateinit var binding:ActivityChattingwindowBinding
    private lateinit var databaseReference: DatabaseReference

    private lateinit var senderRoom:String
    private lateinit var recieverRoom:String

    private lateinit var adapter: chatadapter
    private lateinit var messagelist:ArrayList<messageclass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityChattingwindowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().reference

        val reciverid=intent.getStringExtra("id")
        val sharedPrefs=getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val senderid=sharedPrefs?.getString("staffid","")
        reciverid?.let{retrieve(it)}

        senderRoom=reciverid+senderid
        recieverRoom=senderid+reciverid

        messagelist = ArrayList()
        adapter = chatadapter(this,messagelist,senderid!!)
        val manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        binding.chatwindowrecycler.layoutManager = manager
        binding.chatwindowrecycler.adapter = adapter

        databaseReference.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messagelist.clear()
                    for(postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(messageclass::class.java)
                        messagelist.add(message!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding.sndbtn.setOnClickListener(){
            val message= binding.editTextTextMultiLine.text.toString()
            val messageobj =messageclass(message,senderid)

            databaseReference.child("chats").child(senderRoom!!).child("messages").push().setValue(messageobj).addOnSuccessListener {
                databaseReference.child("chats").child(recieverRoom!!).child("messages").push().setValue(messageobj)
            }

            binding.editTextTextMultiLine.setText("")
        }

        binding.chatwindowback.setOnClickListener(){
            onBackPressed()
        }
    }

    private fun retrieve(reciverid: String) {
        databaseReference.child("faculties").orderByChild("staffId").equalTo(reciverid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userdata = userSnapshot.getValue(userData::class.java)
                            binding.chatwindowname.text = userdata?.name
                            binding.chatwindowemail.text = userdata?.email
                            val photoBase64=userdata?.photoBase64

                            if (!photoBase64.isNullOrEmpty()) {
                                try {
                                    // If your string has a prefix like "data:image/png;base64,", remove it
                                    val base64Data = photoBase64.substringAfter("base64,", photoBase64)

                                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                                    if (bitmap != null) {
                                        binding.chatwindowproimg.setImageBitmap(bitmap)
                                    } else {
                                        Toast.makeText(this@chattingwindow, "Bitmap is null", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(this@chattingwindow, "Error decoding image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }

                        }
                    } else {
                        Toast.makeText(this@chattingwindow, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@chattingwindow, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}