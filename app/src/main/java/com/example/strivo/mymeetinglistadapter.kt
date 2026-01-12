package com.example.strivo

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.databinding.IndividualMeetingBinding
import com.example.strivo.dataclass.meetingdata
import com.example.strivo.dataclass.userData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class mymeetinglistadapter(private val context: Context, private val meetinglist:List<meetingdata>):
    RecyclerView.Adapter<mymeetinglistadapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): mymeetinglistadapter.MyViewHolder {
        val binding=IndividualMeetingBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding,context)
    }

    override fun onBindViewHolder(holder: mymeetinglistadapter.MyViewHolder, position: Int) {
        val meeting= meetinglist[position]
        holder.bind(meeting)

        holder.itemView.setOnClickListener(){
            val intent = Intent(context,Meeting_details::class.java)
            intent.putExtra("meetid", meeting.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return meetinglist.size
    }
    class MyViewHolder(private val binding: IndividualMeetingBinding,private val context: Context):RecyclerView.ViewHolder(binding.root){
        fun bind(meeting: meetingdata){
            binding.meetingdate.text=meeting.date
            binding.meetingdesc.text=meeting.title
        }
    }
}