package com.example.learningapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.learningapp.utils.PdfTextExtractor
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InterviewPreparationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvSkills: TextView
    private lateinit var tvExperience: TextView
    private lateinit var tvEducation: TextView
    private lateinit var tvTargetRole: TextView

    private lateinit var btnUploadResume: MaterialButton
    private lateinit var tvResumeFileName: TextView
    private lateinit var tvResumeStatus: TextView

    private lateinit var actQuestionType: AutoCompleteTextView
    private lateinit var actDifficulty: AutoCompleteTextView
    private lateinit var btnGenerate: MaterialButton

    private var selectedSkills = ""
    private var selectedExperience = ""
    private var selectedEducation = ""
    private var selectedTargetRole = ""

    private var selectedResumeUri: Uri? = null
    private var extractedResumeText = ""

    private val selectPdfLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Continue with URI
                }

                selectedResumeUri = uri
                val fileName = getFileName(uri)

                tvResumeFileName.text = fileName
                tvResumeFileName.setTextColor(Color.parseColor("#F26722"))
                tvResumeStatus.text = "Resume loaded successfully"

                // Extract text from PDF
                extractedResumeText = PdfTextExtractor.extractTextFromPdf(applicationContext, uri)
            }
        }

    private val questionTypes = arrayOf(
        "Technical Questions",
        "HR Questions",
        "Mixed Questions"
    )

    private val difficultyLevels = arrayOf(
        "Easy",
        "Medium",
        "Hard"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interview_preparation)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupListeners()
        setupDropdowns()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserProfile(user.uid)
    }

    private fun initializeViews() {
        tvSkills = findViewById(R.id.tvInterviewSkills)
        tvExperience = findViewById(R.id.tvInterviewExperience)
        tvEducation = findViewById(R.id.tvInterviewEducation)
        tvTargetRole = findViewById(R.id.tvInterviewTargetRole)

        btnUploadResume = findViewById(R.id.btnUploadResume)
        tvResumeFileName = findViewById(R.id.tvResumeFileName)
        tvResumeStatus = findViewById(R.id.tvResumeStatus)

        actQuestionType = findViewById(R.id.actQuestionType)
        actDifficulty = findViewById(R.id.actDifficulty)
        btnGenerate = findViewById(R.id.btnGenerateQuestions)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnInterviewBack).setOnClickListener {
            finish()
        }

        btnUploadResume.setOnClickListener {
            selectPdfLauncher.launch(arrayOf("application/pdf"))
        }

        btnGenerate.setOnClickListener {
            generateQuestions()
        }
    }

    private fun setupDropdowns() {
        val questionAdapter = ArrayAdapter(
            this,
            R.layout.item_interview_dropdown,
            android.R.id.text1,
            questionTypes
        )
        actQuestionType.setAdapter(questionAdapter)

        val requestedType = intent.getStringExtra("questionType") ?: "Technical Questions"
        val validType = if (requestedType in questionTypes) requestedType else "Technical Questions"
        actQuestionType.setText(validType, false)

        actQuestionType.setOnClickListener {
            actQuestionType.showDropDown()
        }

        val difficultyAdapter = ArrayAdapter(
            this,
            R.layout.item_interview_dropdown,
            android.R.id.text1,
            difficultyLevels
        )
        actDifficulty.setAdapter(difficultyAdapter)
        actDifficulty.setText("Medium", false)

        actDifficulty.setOnClickListener {
            actDifficulty.showDropDown()
        }
    }

    private fun loadUserProfile(uid: String) {
        btnGenerate.isEnabled = false

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "Please complete your profile first", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val skillsList = document.get("skills") as? List<*>
                selectedSkills = skillsList?.filterIsInstance<String>()?.joinToString(", ") ?: ""
                selectedExperience = document.getString("experienceLevel") ?: ""
                selectedEducation = document.getString("education") ?: ""
                selectedTargetRole = document.getString("targetRole") ?: ""

                tvSkills.text = selectedSkills.ifBlank { "Not added" }
                tvExperience.text = selectedExperience.ifBlank { "Not added" }
                tvEducation.text = selectedEducation.ifBlank { "Not added" }
                tvTargetRole.text = selectedTargetRole.ifBlank { "Not added" }

                btnGenerate.isEnabled = true
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Unable to load profile: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                btnGenerate.isEnabled = false
            }
    }

    private fun generateQuestions() {
        if (selectedTargetRole.isBlank()) {
            Toast.makeText(this, "Please add your Target Job Role in Edit Profile", Toast.LENGTH_LONG).show()
            return
        }

        val questionType = actQuestionType.text.toString().trim()
        val difficulty = actDifficulty.text.toString().trim()

        if (questionType !in questionTypes) {
            Toast.makeText(this, "Please select a valid question type", Toast.LENGTH_SHORT).show()
            return
        }

        if (difficulty !in difficultyLevels) {
            Toast.makeText(this, "Please select a valid difficulty level", Toast.LENGTH_SHORT).show()
            return
        }

        val fullResumeContext = StringBuilder().apply {
            if (extractedResumeText.isNotBlank()) {
                append("Extracted PDF Resume Content:\n$extractedResumeText\n\n")
            }
            append("Profile Details:\n")
            append("Target Role: $selectedTargetRole\n")
            append("Skills: $selectedSkills\n")
            append("Experience: $selectedExperience\n")
            append("Education: $selectedEducation\n")
            append("Question Category: $questionType\n")
        }.toString()

        val intent = Intent(this, AIResumeInterviewActivity::class.java).apply {
            putExtra("resumeText", fullResumeContext)
            putExtra("targetRole", "$selectedTargetRole ($questionType)")
            putExtra("difficulty", difficulty)
        }
        startActivity(intent)
    }

    private fun getFileName(uri: Uri): String {
        var name = "Resume.pdf"
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}