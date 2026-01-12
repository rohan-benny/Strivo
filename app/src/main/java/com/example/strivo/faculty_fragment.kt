package com.example.strivo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.databinding.FragmentFacultyFragmentBinding
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class faculty_fragment : Fragment() {
    private lateinit var binding: FragmentFacultyFragmentBinding
    private val facultylist= mutableListOf<userData>()
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: FacultyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentFacultyFragmentBinding.inflate(inflater,container,false)
        firebaseDatabase=FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("faculties")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FacultyAdapter(requireContext(),facultylist)
        binding.facultyrecycler.layoutManager=GridLayoutManager(requireContext(),2)
        binding.facultyrecycler.adapter=adapter

        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                facultylist.clear()
                for(dataSnapshot in snapshot.children){
                    val fac=dataSnapshot.getValue(userData::class.java)
                    fac?.let{facultylist.add(it)}
                }
                binding.textView11.text = "${facultylist.size}"
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),"Failed To retrieve data",Toast.LENGTH_SHORT).show()
            }
        })
    }

}