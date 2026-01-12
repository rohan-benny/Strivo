package com.example.strivo

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.databinding.IndividualcircularBinding
import com.example.strivo.dataclass.circulardata

class circularadapter(private val context: Context, private val circularlist:List<circulardata>):
    RecyclerView.Adapter<circularadapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): circularadapter.MyViewHolder {
        val binding=IndividualcircularBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: circularadapter.MyViewHolder, position: Int) {
        val circular= circularlist[position]
        holder.bind(circular)

        holder.itemView.setOnClickListener(){
            val intent = Intent(context,circulardetails::class.java)
            intent.putExtra("circid", circular.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return circularlist.size
    }
    class MyViewHolder(private val binding: IndividualcircularBinding,private val context: Context):RecyclerView.ViewHolder(binding.root){
        fun bind(circular: circulardata){
            binding.textView67.text=circular.title
            binding.textView74.text=circular.date
        }
    }
}