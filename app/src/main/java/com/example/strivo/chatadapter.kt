package com.example.strivo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class chatadapter(
    val context: Context,
    val messagelist: ArrayList<messageclass>,
    val currentuser: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val item_receive = 1
    val item_sent = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == item_receive) {
            val view = LayoutInflater.from(context).inflate(R.layout.receivechatlayout, parent, false)
            ReceiveViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.sendchatlayout, parent, false)
            SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messagelist[position]

        if (holder is SentViewHolder) {
            holder.sentMessage.text = currentMessage.textmessage
        } else if (holder is ReceiveViewHolder) {
            holder.receiveMessage.text = currentMessage.textmessage
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messagelist[position]
        return if (currentuser == currentMessage.sender) {
            item_sent
        } else {
            item_receive
        }
    }

    override fun getItemCount(): Int {
        return messagelist.size
    }

    // Sent message ViewHolder
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.textView77)
    }

    // Received message ViewHolder
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.textView76)
    }
}
