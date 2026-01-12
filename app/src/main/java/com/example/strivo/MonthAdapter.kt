package com.example.strivo

import android.content.Context
import android.graphics.Color // Import the Color class
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.strivo.R // This is still needed for layout IDs
import com.example.strivo.dataclass.CalendarEvent // Correct data class package
import com.example.strivo.dataclass.MonthEvents // Correct data class package

// This adapter is for the main RecyclerView that displays each month block (item_month.xml)
class MonthAdapter(private val context: Context) :
    RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    private var monthList: List<MonthEvents> = emptyList()

    // Function to update the data displayed by the adapter.
    fun submitList(newList: List<MonthEvents>) {
        monthList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val monthEvents = monthList[position]
        holder.bind(monthEvents)
    }

    override fun getItemCount(): Int = monthList.size

    inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthTitle: TextView = itemView.findViewById(R.id.monthTitle)
        val eventsContainer: LinearLayout = itemView.findViewById(R.id.eventsContainer)

        fun bind(monthEvents: MonthEvents) {
            monthTitle.text = monthEvents.monthName
            eventsContainer.removeAllViews()

            monthEvents.events.forEach { event ->
                val eventView = LayoutInflater.from(context).inflate(R.layout.item_event, eventsContainer, false)

                val colorStrip: View = eventView.findViewById(R.id.colorStrip)
                val eventDate: TextView = eventView.findViewById(R.id.eventDate)
                val eventTitle: TextView = eventView.findViewById(R.id.eventTitle)

                eventDate.text = event.displayDate
                eventTitle.text = event.title

                // --- HARDCODED COLORS HERE ---
                val colorHex = when (event.type) {
                    "general" -> "#FFCC80"      // Peach color for general events
                    "holiday" -> "#A5D6A7"      // Green color for holiday events
                    "exam" -> "#90CAF9"         // Blue color for exam events
                    else -> "#FFCC80"           // Default grey color
                }
                colorStrip.setBackgroundColor(Color.parseColor(colorHex)) // Set background color using Color.parseColor

                eventsContainer.addView(eventView)
            }
        }
    }
}