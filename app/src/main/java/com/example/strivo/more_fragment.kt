package com.example.strivo

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.service.autofill.UserData
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.strivo.databinding.ActivityPrivacypolicyBinding
import com.example.strivo.databinding.FragmentMoreFragmentBinding
import com.example.strivo.dataclass.userData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class more_fragment : Fragment() {
    private lateinit var binding: FragmentMoreFragmentBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentMoreFragmentBinding.inflate(inflater,container,false)
        firebaseDatabase= FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("faculties")

        val sharedPrefs=activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val email=sharedPrefs?.getString("loginuser","")
        email?.let{retrieve(it)}

        binding.logoutbtn.setOnClickListener(){
            showCustomDialogBox()
        }

        binding.myprofilebtn.setOnClickListener(){
            val intent=Intent(requireContext(),Myprofile::class.java)
            requireContext().startActivity(intent)
        }

        binding.privacybtn.setOnClickListener(){
            val intent=Intent(requireContext(),privacypolicy::class.java)
            requireContext().startActivity(intent)
        }
        binding.changepsswdbtn.setOnClickListener(){
            val intent=Intent(requireContext(),changepassword::class.java)
            intent.putExtra("email", email)
            requireContext().startActivity(intent)
        }

        return binding.root



    }

    private fun showCustomDialogBox() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.logout_popup)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val button1: Button = dialog.findViewById(R.id.logoutbtn1)
        val button2: Button = dialog.findViewById(R.id.cancelbtn2)


        button1.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            activity?.finish()
            dialog.dismiss() // Corrected line
        }

        button2.setOnClickListener {
            dialog.dismiss()
        }


        dialog.show()

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
                            val role=userdata?.role

                            binding.moreproname.text = name
                            if(role=="HOD"){
                                binding.morehod.text="HOD, Computer Science"
                            }else{
                                binding.morehod.text="Staff, Computer Science"
                            }

                            if (!photoBase64.isNullOrEmpty()) {
                                try {
                                    // If your string has a prefix like "data:image/png;base64,", remove it
                                    val base64Data = photoBase64.substringAfter("base64,", photoBase64)

                                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                                    if (bitmap != null) {
                                        binding.morepropic.setImageBitmap(bitmap)
                                    } else {
                                        Toast.makeText(requireContext(), "Bitmap is null", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(requireContext(), "Error decoding image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else {
                                Toast.makeText(
                                    requireContext(),
                                    "Bitmap is not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        }
                    } else {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


}