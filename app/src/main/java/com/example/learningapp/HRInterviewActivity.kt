package com.example.learningapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.learningapp.api.ApiClient
import com.example.learningapp.api.HRGenerateQuestionsRequest
import com.example.learningapp.api.ResumeExtractRequest
import com.example.learningapp.utils.PdfTextExtractor
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class HRInterviewActivity : AppCompatActivity() {

    // Toolbar
    private lateinit var btnHRBack: MaterialCardView

    // HR categories
    private lateinit var cbIntroduction: MaterialCheckBox
    private lateinit var cbPersonal: MaterialCheckBox
    private lateinit var cbStrengths: MaterialCheckBox
    private lateinit var cbSituational: MaterialCheckBox
    private lateinit var cbCompany: MaterialCheckBox
    private lateinit var cbSalary: MaterialCheckBox

    // Difficulty
    private lateinit var rgHRDifficulty: RadioGroup

    // Resume
    private lateinit var btnHRUploadResume: MaterialButton
    private lateinit var tvHRResumeFileName: TextView
    private lateinit var tvHRResumeStatus: TextView

    // Generate
    private lateinit var btnGenerateHR: MaterialButton

    private var selectedResumeUri: Uri? = null
    private var selectedResumeName = ""

    companion object {
        private const val MAX_RESUME_SIZE = 5L * 1024L * 1024L
    }

    // PDF picker
    private val selectPdfLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

            if (uri != null) {

                if (validatePdf(uri)) {

                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        // Continue with the selected URI.
                    }

                    selectedResumeUri = uri
                    selectedResumeName = getFileName(uri)

                    tvHRResumeFileName.text = selectedResumeName
                    tvHRResumeFileName.setTextColor(
                        Color.parseColor("#F26722")
                    )

                    tvHRResumeStatus.text = "PDF selected successfully"
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hr_interview)

        initializeViews()
        setupToolbar()
        setupResumePicker()
        setupGenerateButton()
    }

    // Initialize XML views
    private fun initializeViews() {

        btnHRBack = findViewById(R.id.btnHRBack)

        cbIntroduction = findViewById(R.id.cbIntroduction)
        cbPersonal = findViewById(R.id.cbPersonal)
        cbStrengths = findViewById(R.id.cbStrengths)
        cbSituational = findViewById(R.id.cbSituational)
        cbCompany = findViewById(R.id.cbCompany)
        cbSalary = findViewById(R.id.cbSalary)

        rgHRDifficulty = findViewById(R.id.rgHRDifficulty)

        btnHRUploadResume = findViewById(R.id.btnHRUploadResume)
        tvHRResumeFileName = findViewById(R.id.tvHRResumeFileName)
        tvHRResumeStatus = findViewById(R.id.tvHRResumeStatus)

        btnGenerateHR = findViewById(R.id.btnGenerateHR)
    }

    // Back button
    private fun setupToolbar() {

        btnHRBack.setOnClickListener {
            finish()
        }
    }

    // Resume picker
    private fun setupResumePicker() {

        btnHRUploadResume.setOnClickListener {
            selectPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }

    // Validate selected PDF
    private fun validatePdf(uri: Uri): Boolean {

        val fileName = getFileName(uri)
        val mimeType = contentResolver.getType(uri)

        if (!fileName.endsWith(".pdf", ignoreCase = true) ||
            (mimeType != null && mimeType != "application/pdf")
        ) {

            Toast.makeText(
                this,
                "Please select a PDF file only",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }

        val fileSize = getFileSize(uri)

        if (fileSize <= 0L) {

            Toast.makeText(
                this,
                "Unable to read PDF file",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }

        if (fileSize > MAX_RESUME_SIZE) {

            Toast.makeText(
                this,
                "Resume must be smaller than 5 MB",
                Toast.LENGTH_LONG
            ).show()

            return false
        }

        return true
    }

    // Get filename
    private fun getFileName(uri: Uri): String {

        var name = "Selected_Resume.pdf"

        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->

            val nameIndex =
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }

        return name
    }

    // Get file size
    private fun getFileSize(uri: Uri): Long {

        return try {

            contentResolver.query(
                uri,
                arrayOf(OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->

                if (cursor.moveToFirst()) {

                    val sizeIndex =
                        cursor.getColumnIndex(OpenableColumns.SIZE)

                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        cursor.getLong(sizeIndex)
                    } else {
                        -1L
                    }

                } else {
                    -1L
                }

            } ?: -1L

        } catch (e: Exception) {
            -1L
        }
    }

    // Collect selected HR categories
    private fun collectSelectedFocusAreas(): List<String> {

        val topics = mutableListOf<String>()

        if (cbIntroduction.isChecked) {
            topics.add("Self Introduction")
        }

        if (cbPersonal.isChecked) {
            topics.add("Personal and Background Questions")
        }

        if (cbStrengths.isChecked) {
            topics.add("Strengths and Weaknesses")
        }

        if (cbSituational.isChecked) {
            topics.add("Situational Questions")
        }

        if (cbCompany.isChecked) {
            topics.add("Company and Job Role Questions")
        }

        if (cbSalary.isChecked) {
            topics.add("Salary and Career Goals")
        }

        return topics
    }

    // Get difficulty
    private fun getSelectedDifficulty(): String {

        val selectedId = rgHRDifficulty.checkedRadioButtonId

        return if (selectedId != -1) {

            findViewById<RadioButton>(selectedId).text.toString()

        } else {
            "Easy"
        }
    }

    // Generate HR questions button setup
    private fun setupGenerateButton() {

        btnGenerateHR.setOnClickListener {

            val selectedTopics = collectSelectedFocusAreas()

            if (selectedTopics.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please select at least one HR category",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val difficulty = getSelectedDifficulty()

            startPracticeSession(
                selectedTopics,
                difficulty,
                selectedResumeUri
            )
        }
    }

    // Start practice session with backend API integration
    private fun startPracticeSession(
        topics: List<String>,
        difficulty: String,
        resumeUri: Uri?
    ) {
        btnGenerateHR.isEnabled = false
        btnGenerateHR.text = "Initializing AI Interview..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                var extractedText = ""

                // Extract text from the PDF resume (time-limited so the UI never hangs)
                if (resumeUri != null) {
                    extractedText = withTimeoutOrNull(20_000) {
                        PdfTextExtractor.extractTextFromPdf(applicationContext, resumeUri)
                    }.orEmpty()

                    if (extractedText.isBlank()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@HRInterviewActivity,
                                "Couldn't read text from this PDF (scanned?). Continuing without resume.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                val roleTitle = if (topics.isNotEmpty()) {
                    "HR Interview (${topics.joinToString(", ")})"
                } else {
                    "HR Interview Practice"
                }

                withContext(Dispatchers.Main) {
                    resetGenerateButton()

                    // Launch Groq AI 1-by-1 Interview Session
                    val intent = Intent(this@HRInterviewActivity, AIResumeInterviewActivity::class.java).apply {
                        putExtra("resumeText", extractedText)
                        putExtra("targetRole", roleTitle)
                        putExtra("difficulty", difficulty)
                    }
                    startActivity(intent)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resetGenerateButton()
                    showError("Error starting AI Interview: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun resetGenerateButton() {
        btnGenerateHR.isEnabled = true
        btnGenerateHR.text = "Generate HR Questions  →"
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("AI Question Generation")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}