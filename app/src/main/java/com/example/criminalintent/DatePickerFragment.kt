package com.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import java.util.*

class DatePickerFragment : DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dateListener = DatePickerDialog.OnDateSetListener { _: DatePicker,
                                                                year: Int,
                                                                month: Int,
                                                                day: Int ->

            val resultDate: Date = GregorianCalendar(year, month, day).time
            setFragmentResult("dateRequest", bundleOf(DIALOG_DATE to resultDate))
        }

        val calendar = Calendar.getInstance()
        val date = requireArguments().getSerializable(ARG_DATE) as Date

        calendar.time = date

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)


        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )

    }

    companion object {
        const val DIALOG_DATE = "DialogDate"
        const val ARG_DATE = "date"
    }
}