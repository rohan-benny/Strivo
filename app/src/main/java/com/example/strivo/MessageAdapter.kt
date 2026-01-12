package com.example.strivo

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.databinding.IndividualchatlistBinding
import com.example.strivo.dataclass.eventdata
import com.example.strivo.dataclass.userData

class MessageAdapter(private val context: Context, private val faclist:List<userData>):
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding=IndividualchatlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding,context)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val fac=faclist[position]
        holder.bind(fac)

        holder.itemView.setOnClickListener(){
            val intent = Intent(context,chattingwindow::class.java)
            intent.putExtra("id",fac.staffId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return faclist.size
    }

    class MyViewHolder(private val binding: IndividualchatlistBinding, private val context: Context):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(faculty: userData){
            binding.indchatname.text=faculty.name
            val photoBase64=faculty.photoBase64

            if (!photoBase64.isNullOrEmpty()) {
                try {
                    // If your string has a prefix like "data:image/png;base64,", remove it
                    val base64Data = photoBase64.substringAfter("base64,", photoBase64)

                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    if (bitmap != null) {
                        binding.indchatimg.setImageBitmap(bitmap)
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