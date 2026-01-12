package com.example.strivo

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.databinding.IndividualMeetingBinding
import com.example.strivo.databinding.IndividualeventBinding
import com.example.strivo.dataclass.eventdata
import com.example.strivo.dataclass.meetingdata

class EventAdapter(private val context: Context, private val eventlist:List<eventdata>):
    RecyclerView.Adapter<EventAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.MyViewHolder {
        val binding=IndividualeventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventAdapter.MyViewHolder(binding,context)
    }


    override fun onBindViewHolder(holder: EventAdapter.MyViewHolder, position: Int) {
        val event=eventlist[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return eventlist.size
    }

    class MyViewHolder(private val binding: IndividualeventBinding,private val context: Context):RecyclerView.ViewHolder(binding.root) {
        fun bind(event:eventdata){
            binding.eventname.text=event.title
            binding.textView69.text=event.startdate
            val id = event.id

            binding.button.setOnClickListener(){
                val intent=Intent(context,Eventdetailsview::class.java)
                intent.putExtra("eventid",id)
                context.startActivity(intent)
            }
        }

    }
}