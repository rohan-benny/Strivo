package com.example.strivo

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.databinding.FacultyCardBinding
import com.example.strivo.dataclass.userData

class FacultyAdapter(private val context:Context, private val facultylist:List<userData>):
    RecyclerView.Adapter<FacultyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyAdapter.MyViewHolder {
        val binding=FacultyCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding,context)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val faculty=facultylist[position]
        holder.bind(faculty)
    }

    override fun getItemCount(): Int {
        return facultylist.size
    }

    class MyViewHolder(private val binding: FacultyCardBinding, private val context: Context): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(faculty: userData) {
            binding.facprlistname.text=faculty.name
            binding.facprlistemail.text=faculty.email
            binding.facprlistroom.text=faculty.staffRoomId
             val photoBase64=faculty.photoBase64

            if (!photoBase64.isNullOrEmpty()) {
                try {
                    // If your string has a prefix like "data:image/png;base64,", remove it
                    val base64Data = photoBase64.substringAfter("base64,", photoBase64)

                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    if (bitmap != null) {
                        binding.fcprimg.setImageBitmap(bitmap)
                    } else {
                        Toast.makeText(context, "Bitmap is null", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error decoding image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

}