package com.example.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.room.Update
import java.util.*

class TimePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val date = requireArguments().getSerializable(DatePickerFragment.ARG_DATE) as Date

        calendar.time = date

        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val resultTime : Date = GregorianCalendar(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                hourOfDay,
                minute
            ).time

            setFragmentResult("dateRequest", bundleOf(DIALOG_DATE to resultTime))
        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHour,
            initialMinute,
            true
        )
    }

    companion object {
        const val DIALOG_DATE = "DialogDate"
    }
}