package com.example.learningapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.learningapp.api.GroqApiClient
import com.example.learningapp.api.GroqMessage
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class TechnicalMcq(
    val questionNumber: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String,
    val topic: String
)

class GeneratedQuestionsActivity : AppCompatActivity() {

    private lateinit var tvTechnology: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvQuestionCount: TextView

    private lateinit var cardPlaceholder: MaterialCardView
    private lateinit var progressGenerating: ProgressBar
    private lateinit var tvPlaceholderDescription: TextView

    private lateinit var layoutQuizContent: View
    private lateinit var tvQuestionProgress: TextView
    private lateinit var questionProgressBar: ProgressBar
    private lateinit var tvCurrentQuestion: TextView
    private lateinit var rgQuestionOptions: RadioGroup

    private lateinit var rbOptionA: RadioButton
    private lateinit var rbOptionB: RadioButton
    private lateinit var rbOptionC: RadioButton
    private lateinit var rbOptionD: RadioButton

    private lateinit var btnPreviousQuestion: View
    private lateinit var btnNextQuestion: View
    private lateinit var btnSubmitTechnicalQuiz: View

    private var technology = ""
    private var difficulty = "Medium"
    private var questionCount = 5

    private val questions = mutableListOf<TechnicalMcq>()
    private val selectedAnswers = mutableMapOf<Int, Int>()

    private var currentQuestionIndex = 0
    private var quizSubmitted = false

    private val optionButtons: List<RadioButton>
        get() = listOf(rbOptionA, rbOptionB, rbOptionC, rbOptionD)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generated_questions)

        initializeViews()

        technology = intent.getStringExtra("technology") ?: ""
        difficulty = intent.getStringExtra("difficulty") ?: "Medium"
        questionCount = intent.getIntExtra("questionCount", 5)

        findViewById<View>(R.id.btnGeneratedBack).setOnClickListener {
            finish()
        }

        tvTechnology.text = "Technology: $technology"
        tvDifficulty.text = "Difficulty: $difficulty"
        tvQuestionCount.text = "Questions: $questionCount"

        setupListeners()

        if (technology.isBlank()) {
            showError("Technology was not selected. Please go back.")
            return
        }

        if (questionCount !in listOf(5, 10, 15, 20, 25)) {
            showError("Invalid question count. Please go back and select again.")
            return
        }

        generateTechnicalQuestions()
    }

    private fun initializeViews() {
        tvTechnology = findViewById(R.id.tvGeneratedTechnology)
        tvDifficulty = findViewById(R.id.tvGeneratedDifficulty)
        tvQuestionCount = findViewById(R.id.tvGeneratedQuestionCount)

        cardPlaceholder = findViewById(R.id.cardQuestionsPlaceholder)
        progressGenerating = findViewById(R.id.progressGenerating)
        tvPlaceholderDescription = findViewById(R.id.tvPlaceholderDescription)

        layoutQuizContent = findViewById(R.id.layoutQuizContent)

        tvQuestionProgress = findViewById(R.id.tvQuestionProgress)
        questionProgressBar = findViewById(R.id.questionProgressBar)
        tvCurrentQuestion = findViewById(R.id.tvCurrentQuestion)

        rgQuestionOptions = findViewById(R.id.rgQuestionOptions)

        rbOptionA = findViewById(R.id.rbOptionA)
        rbOptionB = findViewById(R.id.rbOptionB)
        rbOptionC = findViewById(R.id.rbOptionC)
        rbOptionD = findViewById(R.id.rbOptionD)

        btnPreviousQuestion = findViewById(R.id.btnPreviousQuestion)
        btnNextQuestion = findViewById(R.id.btnNextQuestion)
        btnSubmitTechnicalQuiz = findViewById(R.id.btnSubmitTechnicalQuiz)
    }

    private fun setupListeners() {

        rgQuestionOptions.setOnCheckedChangeListener { _, checkedId ->

            if (quizSubmitted || questions.isEmpty()) {
                return@setOnCheckedChangeListener
            }

            val selectedIndex = optionButtons.indexOfFirst {
                it.id == checkedId
            }

            if (selectedIndex >= 0) {
                selectedAnswers[currentQuestionIndex] = selectedIndex
            }
        }

        btnPreviousQuestion.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                displayQuestion()
            }
        }

        btnNextQuestion.setOnClickListener {
            if (currentQuestionIndex < questions.lastIndex) {
                currentQuestionIndex++
                displayQuestion()
            }
        }

        btnSubmitTechnicalQuiz.setOnClickListener {
            submitQuiz()
        }
    }

    // --------------------------------------------------
    // GENERATE QUESTIONS
    // --------------------------------------------------

    private fun generateTechnicalQuestions() {

        showLoading()

        lifecycleScope.launch {

            try {

                val generatedQuestions = withContext(Dispatchers.IO) {

                    val prompt = """
                        You are an expert technical interviewer and MCQ creator.

                        Generate exactly $questionCount multiple-choice questions.

                        Technology: $technology
                        Difficulty: $difficulty

                        REQUIREMENTS:
                        1. Generate exactly $questionCount questions.
                        2. Every question must relate to $technology.
                        3. Match the selected difficulty.
                        4. Each question must have exactly four options.
                        5. Only one option must be correct.
                        6. Provide a clear explanation.
                        7. Include a topic name.
                        8. Avoid duplicate questions.
                        9. Do not depend on personal candidate information.
                        10. Return valid JSON only.

                        correctAnswer must be a zero-based index:
                        0 = Option A
                        1 = Option B
                        2 = Option C
                        3 = Option D

                        Return this structure:

                        {
                          "questions": [
                            {
                              "questionNumber": 1,
                              "question": "Question text",
                              "options": [
                                "Option A",
                                "Option B",
                                "Option C",
                                "Option D"
                              ],
                              "correctAnswer": 0,
                              "explanation": "Explanation",
                              "topic": "Topic name"
                            }
                          ]
                        }
                    """.trimIndent()

                    val messages = listOf(
                        GroqMessage(
                            role = "system",
                            content = """
                                You are a professional technical MCQ generator.
                                Return valid JSON only.
                                Generate exactly the requested number of questions.
                            """.trimIndent()
                        ),
                        GroqMessage(
                            role = "user",
                            content = prompt
                        )
                    )

                    val response =
                        GroqApiClient.executeChatCompletionWithFallback(
                            messages = messages,
                            temperature = 0.3
                        )

                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        throw Exception("Groq API error: HTTP ${response.code()} - $errorBody")
                    }

                    val content = response.body()
                        ?.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?: throw Exception("Groq returned an empty response.")

                    parseQuestions(content)
                }

                questions.clear()
                questions.addAll(generatedQuestions)

                selectedAnswers.clear()
                currentQuestionIndex = 0
                quizSubmitted = false

                displayQuestion()

                cardPlaceholder.visibility = View.GONE
                layoutQuizContent.visibility = View.VISIBLE

            } catch (e: Exception) {
                showError(
                    e.localizedMessage ?: "Unable to generate questions."
                )
            }
        }
    }

    // --------------------------------------------------
    // PARSE AI RESPONSE
    // --------------------------------------------------

    private fun parseQuestions(content: String): List<TechnicalMcq> {

        val cleaned = content
            .trim()
            .replace(
                Regex("^```(?:json)?\\s*", RegexOption.IGNORE_CASE),
                ""
            )
            .replace(Regex("\\s*```$"), "")
            .trim()

        val start = cleaned.indexOf("{")
        val end = cleaned.lastIndexOf("}")

        if (start < 0 || end <= start) {
            throw Exception("Groq returned invalid JSON.")
        }

        val jsonObject = JSONObject(cleaned.substring(start, end + 1))
        val array = jsonObject.optJSONArray("questions")
            ?: throw Exception("Questions array is missing.")

        if (array.length() == 0) {
            throw Exception("No questions generated by AI.")
        }

        val result = mutableListOf<TechnicalMcq>()

        for (i in 0 until array.length()) {

            val item = array.getJSONObject(i)

            val question = item.optString("question").trim()

            if (question.isBlank()) {
                throw Exception("Question ${i + 1} is empty.")
            }

            val optionsArray = item.optJSONArray("options")
                ?: throw Exception("Options missing in question ${i + 1}.")

            if (optionsArray.length() != 4) {
                throw Exception(
                    "Question ${i + 1} must have exactly four options."
                )
            }

            val options = (0 until 4).map { index ->
                optionsArray.optString(index).trim()
            }

            if (options.any { it.isBlank() }) {
                throw Exception("An option is empty in question ${i + 1}.")
            }

            val correctAnswer = item.optInt("correctAnswer", -1)

            if (correctAnswer !in 0..3) {
                throw Exception(
                    "Invalid correct answer in question ${i + 1}."
                )
            }

            val explanation = item.optString("explanation").trim()

            if (explanation.isBlank()) {
                throw Exception(
                    "Explanation missing in question ${i + 1}."
                )
            }

            val topic = item.optString("topic", "General").trim()

            result.add(
                TechnicalMcq(
                    questionNumber = i + 1,
                    question = question,
                    options = options,
                    correctAnswer = correctAnswer,
                    explanation = explanation,
                    topic = topic.ifBlank { "General" }
                )
            )
        }

        return result
    }

    // --------------------------------------------------
    // DISPLAY ONE QUESTION
    // --------------------------------------------------

    private fun displayQuestion() {

        if (questions.isEmpty()) return

        val question = questions[currentQuestionIndex]

        tvQuestionProgress.text =
            "Question ${currentQuestionIndex + 1} of ${questions.size}"

        questionProgressBar.max = questions.size
        questionProgressBar.progress = currentQuestionIndex + 1

        tvCurrentQuestion.text = question.question

        // Remove the old selection before setting the current one.
        rgQuestionOptions.setOnCheckedChangeListener(null)
        rgQuestionOptions.clearCheck()

        question.options.forEachIndexed { index, option ->
            optionButtons[index].text = option
            optionButtons[index].isEnabled = true
        }

        selectedAnswers[currentQuestionIndex]?.let { selected ->
            optionButtons[selected].isChecked = true
        }

        // Restore the listener after rendering.
        rgQuestionOptions.setOnCheckedChangeListener { _, checkedId ->

            if (quizSubmitted || questions.isEmpty()) {
                return@setOnCheckedChangeListener
            }

            val selectedIndex = optionButtons.indexOfFirst {
                it.id == checkedId
            }

            if (selectedIndex >= 0) {
                selectedAnswers[currentQuestionIndex] = selectedIndex
            }
        }

        btnPreviousQuestion.isEnabled = currentQuestionIndex > 0
        btnNextQuestion.isEnabled = currentQuestionIndex < questions.lastIndex

        btnSubmitTechnicalQuiz.visibility =
            if (currentQuestionIndex == questions.lastIndex) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    // --------------------------------------------------
    // SUBMIT AND OPEN RESULT PAGE
    // --------------------------------------------------

    private fun submitQuiz() {

        if (quizSubmitted || questions.isEmpty()) return

        val unansweredCount = questions.size - selectedAnswers.size

        if (unansweredCount > 0) {
            Toast.makeText(
                this,
                "Please answer all questions. $unansweredCount unanswered.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        quizSubmitted = true

        // The score is calculated locally using the stored answer key.
        val score = questions.indices.count { index ->
            selectedAnswers[index] == questions[index].correctAnswer
        }

        val reviewArray = JSONArray()

        questions.forEachIndexed { index, question ->

            val selectedIndex = selectedAnswers[index] ?: return@forEachIndexed

            val item = JSONObject().apply {
                put("questionNumber", index + 1)
                put("question", question.question)
                put("topic", question.topic)
                put("selectedAnswer", question.options[selectedIndex])
                put("correctAnswer", question.options[question.correctAnswer])
                put(
                    "isCorrect",
                    selectedIndex == question.correctAnswer
                )
                put("explanation", question.explanation)
            }

            reviewArray.put(item)
        }

        val resultIntent = Intent(
            this,
            TechnicalMcqResultActivity::class.java
        ).apply {
            putExtra("technology", technology)
            putExtra("difficulty", difficulty)
            putExtra("questionCount", questions.size)
            putExtra("score", score)
            putExtra("review", reviewArray.toString())
        }

        startActivity(resultIntent)
        finish()
    }

    // --------------------------------------------------
    // LOADING AND ERROR
    // --------------------------------------------------

    private fun showLoading() {
        cardPlaceholder.visibility = View.VISIBLE
        layoutQuizContent.visibility = View.GONE
        progressGenerating.visibility = View.VISIBLE

        tvPlaceholderDescription.text =
            "Groq AI is generating $questionCount $difficulty-level " +
                    "$technology MCQ questions. Please wait..."
    }

    private fun showError(message: String) {
        progressGenerating.visibility = View.GONE
        cardPlaceholder.visibility = View.VISIBLE
        layoutQuizContent.visibility = View.GONE

        tvPlaceholderDescription.text =
            "Unable to generate questions.\n\n$message"

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}