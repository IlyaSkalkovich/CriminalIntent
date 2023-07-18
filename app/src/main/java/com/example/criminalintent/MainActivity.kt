package com.example.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import java.util.*


class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCrimeSelected(crimeId: UUID) {
        findNavController(R.id.fragment_container).navigate(
            R.id.action_crimeListFragment_to_crimeFragment,
            bundleOf(CrimeListFragment.ARG_CRIME_ID to crimeId)
        )
    }
}