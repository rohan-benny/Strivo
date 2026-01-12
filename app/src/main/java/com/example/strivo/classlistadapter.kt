package com.example.strivo

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.databinding.IndividualStudentListBinding
import com.example.strivo.dataclass.classlistdata

class classlistadapter(private val context: Context, private val classlist:List<classlistdata>):RecyclerView.Adapter<classlistadapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): classlistadapter.MyViewHolder {
        val binding=IndividualStudentListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding,context)
    }

    override fun onBindViewHolder(holder: classlistadapter.MyViewHolder, position: Int) {
        val classl = classlist[position]
        holder.bind(classl)
    }

    override fun getItemCount(): Int {
        return classlist.size
    }

    class MyViewHolder(private val binding: IndividualStudentListBinding,private val context: Context): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(classlist:classlistdata) {
            binding.studname.text=classlist.name
            binding.rollno.text=classlist.studentId
            val photoBase64=classlist.photo

            if (!photoBase64.isNullOrEmpty()) {
                try {
                    // If your string has a prefix like "data:image/png;base64,", remove it
                    val base64Data = photoBase64.substringAfter("base64,", photoBase64)

                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    if (bitmap != null) {
                        binding.studimg.setImageBitmap(bitmap)
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