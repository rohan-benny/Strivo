package com.example.strivo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.dataclass.RoomData // Assuming you've fixed this to be a data class
import com.example.strivo.databinding.IndividualroomBinding // IMPORT THE GENERATED BINDING CLASS
import java.util.Locale

class RoomAdapter(private val roomList: ArrayList<RoomData>) :
    RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    // --- CHANGE: RoomViewHolder now takes IndividualroomBinding ---
    class RoomViewHolder(private val binding: IndividualroomBinding, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) { // ViewHolder's itemView is binding.root

        // Access views directly from the binding object
        val roomName: TextView = binding.textView91
        val roomCategoryPrefix: TextView = binding.textView92
        val roomCategory: TextView = binding.textView93
        val roomCapacity: TextView = binding.textView94
        val chairIcon: ImageView = binding.imageView45

        init {
            binding.root.setOnClickListener { // Set listener on the root of the item layout
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(adapterPosition)
                }
            }
        }
    }

    // --- CHANGE: onCreateViewHolder now inflates using the binding class ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = IndividualroomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding, mListener)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val currentRoom = roomList[position]

        holder.roomName.text = currentRoom.name
        holder.roomCategory.text = currentRoom.type?.capitalizeWords()
        holder.roomCapacity.text = currentRoom.capacity
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") {
            it.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
    }
}