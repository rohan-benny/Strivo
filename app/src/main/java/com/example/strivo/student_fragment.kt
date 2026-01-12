package com.example.strivo

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.strivo.databinding.FragmentStudentFragmentBinding

class student_fragment : Fragment() {
    private lateinit var binding: FragmentStudentFragmentBinding
    private var year1=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize binding with the layout for this fragment
        binding = FragmentStudentFragmentBinding.inflate(inflater, container, false)
        val autoCompleteTextView = binding.autocomplete
        val items = resources.getStringArray(R.array.year)
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, items)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            year1 = when (selected) {
                "1st Year" -> "1st"
                "2nd Year" -> "2nd"
                else -> ""
            }
        }

        binding.mcaabutton.setOnClickListener {
            val intent = Intent(requireContext(), Classlist::class.java)
            intent.putExtra("year", year1) // Replace with actual data
            intent.putExtra("class", "MCA A")
            requireContext().startActivity(intent)
        }

        binding.mcabbutton2.setOnClickListener {
            val intent = Intent(requireContext(), Classlist::class.java)
            intent.putExtra("year", year1) // Replace with actual data
            intent.putExtra("class", "MCA B")
            requireContext().startActivity(intent)
        }

        binding.mcacbutton4.setOnClickListener {
            val intent = Intent(requireContext(), Classlist::class.java)
            intent.putExtra("year", year1) // Replace with actual data
            intent.putExtra("class", "MCA C")
            requireContext().startActivity(intent)
        }

        binding.mcadbutton6.setOnClickListener {
            val intent = Intent(requireContext(), Classlist::class.java)
            intent.putExtra("year", year1) // Replace with actual data
            intent.putExtra("class", "MCA D")
            requireContext().startActivity(intent)
        }

        binding.msccsbutton8.setOnClickListener {
            val intent = Intent(requireContext(), Classlist::class.java)
            intent.putExtra("year", year1) // Replace with actual data
            intent.putExtra("class", "MSc CS")
            requireContext().startActivity(intent)
        }

        binding.mscdsbutton9.setOnClickListener {
            val intent = Intent(requireContext(), Classlist::class.java)
            intent.putExtra("year", year1) // Replace with actual data
            intent.putExtra("class", "MSc DS")
            requireContext().startActivity(intent)
        }


        return binding.root
    }
}