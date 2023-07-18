package com.example.criminalintent.model

import androidx.lifecycle.ViewModel
import com.example.criminalintent.CrimeRepository

class CrimeListViewModel: ViewModel() {
    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

    fun deleteCrime(crime: Crime) {
        crimeRepository.deleteCrime(crime)
    }
}
