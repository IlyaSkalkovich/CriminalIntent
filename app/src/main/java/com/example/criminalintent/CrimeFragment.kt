package com.example.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.criminalintent.model.Crime
import com.example.criminalintent.model.CrimeDetailViewModel
import com.example.criminalintent.model.Suspect
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeFragment"
private const val REQUEST_PHOTO = 2
private const val fileProviderAuthority = "com.example.criminalintent.fileprovider"
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment: Fragment() {
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    //private lateinit var deleteButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var contactPickerLauncher: ActivityResultLauncher<Intent>

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crime = Crime()

        val crimeId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requireArguments().getSerializable(CrimeListFragment.ARG_CRIME_ID, UUID::class.java) as UUID
        else requireArguments().getSerializable(CrimeListFragment.ARG_CRIME_ID) as UUID

        crimeDetailViewModel.loadCrime(crimeId)

        Log.d(TAG, "args bundle crime ID: $crimeId")
        Log.d(TAG, "onCreate")

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            when {
                result.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
                result.data != null -> {
                    requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    updatePhotoView()
                }
            }
        }

        contactPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    result ->
            when {
                result.resultCode != Activity.RESULT_OK -> return@registerForActivityResult

                result.data != null -> {
                    val contactUri = result.data?.data

                    val queryFields = arrayOf(
                        ContactsContract.Contacts.DISPLAY_NAME)

                    val cursor = contactUri?.let { uri ->
                        requireActivity().contentResolver.query(
                            uri,
                            queryFields,
                            null,
                            null,
                            null) }

                    cursor?.use { contact ->
                        if (contact.count == 0) return@registerForActivityResult

                        contact.moveToFirst()

                            val suspectName = contact.getString(0)
                            val suspect = Suspect(suspectName, "not implemented :(")

                            crime.suspect = suspect
                            crimeDetailViewModel.saveCrime(crime)

                            suspectButton.text = suspect.suspectName

                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        //deleteButton = view.findViewById(R.id.delete_button) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner) {
            it?.let {
                this.crime = it
                photoFile = crimeDetailViewModel.getPhotoFile(it)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    fileProviderAuthority,
                    photoFile)

                updateUI()
            }
        }
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap) }

        else photoView.setImageDrawable(null)

    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

        setFragmentResultListener("dateRequest") { _, bundle ->
            val resultDate = bundle.getSerializable(DatePickerFragment.DIALOG_DATE) as Date

            crime.date = resultDate
            updateUI()
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val resolvedActivity = packageManager.resolveActivity(
                captureImage,
                PackageManager.MATCH_DEFAULT_ONLY
            )

            if (resolvedActivity == null) isEnabled = false

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                cameraLauncher.launch(captureImage)
            }
        }

        solvedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
        }

        dateButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_crimeFragment_to_datePickerFragment,
                bundleOf(DatePickerFragment.ARG_DATE to crime.date)
            )

            /*findNavController().navigate(
                R.id.action_crimeFragment_to_timePickerFragment,
                bundleOf(DatePickerFragment.ARG_DATE to crime.date)
            )*/
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))

                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                contactPickerLauncher.launch(pickContactIntent)
            }

            val packageManager: PackageManager =
                requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) isEnabled = false


        }

        /*deleteButton.apply {
            crimeDetailViewModel.deleteCrime(crime)
        }*/

        val titleWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
// Это пространство оставлено пустым специально
            }
            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
// И это
            }
        }

        titleField.addTextChangedListener(titleWatcher)
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = SimpleDateFormat("yyyy/MMM/dd hh:mm").format(crime.date)

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        if (crime.suspect != null) {
            suspectButton.text = crime.suspect?.suspectName
        }

        updatePhotoView()
    }

    private fun getCrimeReport(): String {
        val solvedString =
            if (crime.isSolved) getString(R.string.crime_report_solved)
            else getString(R.string.crime_report_unsolved)

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        val suspect =
            if (crime.suspect == null) getString(R.string.crime_report_no_suspect)
            else getString(R.string.crime_report_suspect, crime.suspect?.suspectName)

        return getString(R.string.crime_report,
            crime.title, dateString,
            solvedString, suspect)
    }
}