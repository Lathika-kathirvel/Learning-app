package com.example.learningapp

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.learningapp.api.GroqApiClient
import com.example.learningapp.api.GroqMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AptitudeQuestionsActivity : AppCompatActivity() {

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int
    )

    private lateinit var tvTopic: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnNext: MaterialButton

    private lateinit var optionCards: List<MaterialCardView>
    private lateinit var optionTexts: List<TextView>

    private var questions = listOf<Question>()
    private var currentQuestionIndex = 0
    private var selectedAnswer = -1
    private var score = 0
    private var answered = false
    private var quizFinished = false

    private var questionCount = 10
    private var topic = "Quantitative Aptitude"
    private var difficulty = "Medium"

    private var timer: CountDownTimer? = null

    private val blue = Color.parseColor("#3157D5")
    private val white = Color.WHITE
    private val lightBlue = Color.parseColor("#E8EEFF")
    private val grey = Color.parseColor("#DCE3EF")
    private val green = Color.parseColor("#16865A")
    private val lightGreen = Color.parseColor("#E5F6EE")
    private val red = Color.parseColor("#D93025")
    private val lightRed = Color.parseColor("#FCE8E6")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aptitude_questions)

        topic = intent.getStringExtra("aptitudeTopic")
            ?: "Quantitative Aptitude"

        difficulty = intent.getStringExtra("difficulty")
            ?: "Medium"

        questionCount = intent.getIntExtra("questionCount", 10)
            .coerceIn(10, 20)

        initializeViews()
        loadQuestions()

        findViewById<MaterialCardView>(R.id.btnQuizBack)
            .setOnClickListener {
                showExitConfirmation()
            }

        btnNext.setOnClickListener {
            handleNextQuestion()
        }
    }

    private fun initializeViews() {

        tvTopic = findViewById(R.id.tvQuizTopic)
        tvTimer = findViewById(R.id.tvQuizTimer)
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber)
        tvScore = findViewById(R.id.tvQuizScore)
        tvQuestion = findViewById(R.id.tvAptitudeQuestion)
        progressBar = findViewById(R.id.quizProgress)
        btnNext = findViewById(R.id.btnNextQuestion)

        optionCards = listOf(
            findViewById(R.id.optionCard1),
            findViewById(R.id.optionCard2),
            findViewById(R.id.optionCard3),
            findViewById(R.id.optionCard4)
        )

        optionTexts = listOf(
            findViewById(R.id.tvOption1),
            findViewById(R.id.tvOption2),
            findViewById(R.id.tvOption3),
            findViewById(R.id.tvOption4)
        )

        tvTopic.text = "$topic • $difficulty"

        optionCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectAnswer(index)
            }
        }
    }

    // --------------------------------------------------
    // AI QUESTION GENERATION (Groq)
    // --------------------------------------------------

    private fun loadQuestions() {

        setLoading(true)

        lifecycleScope.launch {
            try {
                questions = withContext(Dispatchers.IO) {
                    fetchQuestionsFromAi()
                }

                setLoading(false)
                displayQuestion()
                startTimer()

            } catch (e: Exception) {
                showLoadError(
                    e.localizedMessage ?: "Unable to generate questions."
                )
            }
        }
    }

    private fun setLoading(loading: Boolean) {

        val visibility = if (loading) View.GONE else View.VISIBLE

        optionCards.forEach { it.visibility = visibility }
        btnNext.isEnabled = !loading

        if (loading) {
            tvQuestion.text =
                "Groq AI is generating $questionCount $difficulty " +
                        "$topic questions. Please wait..."
            tvQuestionNumber.text = "Preparing quiz..."
            tvTimer.text = "--:--"
        }
    }

    private fun showLoadError(message: String) {

        tvQuestion.text = "Could not generate questions."

        AlertDialog.Builder(this)
            .setTitle("AI Question Generation")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ -> loadQuestions() }
            .setNegativeButton("Back") { _, _ -> finish() }
            .show()
    }

    private suspend fun fetchQuestionsFromAi(): List<Question> {

        val topicGuide = when (topic) {
            "Quantitative Aptitude" ->
                "percentages, ratio, profit and loss, time and work, " +
                        "speed-distance-time, simple/compound interest, averages"
            "Logical Reasoning" ->
                "number and letter series, coding-decoding, blood relations, " +
                        "directions, syllogisms, seating arrangement"
            "Verbal Ability" ->
                "synonyms, antonyms, sentence correction, fill in the blanks, " +
                        "reading comprehension, idioms"
            "Data Interpretation" ->
                "short data tables/ratios/percent change described in the " +
                        "question text itself (no images)"
            "Puzzles" ->
                "number puzzles, age problems, logic puzzles solvable from " +
                        "the question text alone"
            else -> topic
        }

        val prompt = """
            Generate exactly $questionCount multiple-choice aptitude questions
            for a placement/job interview test.

            Topic: $topic ($topicGuide)
            Difficulty: $difficulty

            Rules:
            - Every question must be self-contained text (no images or charts).
            - Each question has exactly 4 options.
            - Exactly one option is correct. Double-check all calculations.
            - correctAnswer is the zero-based index (0-3) of the correct option.
            - Do not repeat questions.
            - Return valid JSON only, no markdown, in this structure:

            {
              "questions": [
                {
                  "question": "Question text",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correctAnswer": 0
                }
              ]
            }
        """.trimIndent()

        val messages = listOf(
            GroqMessage(
                role = "system",
                content = "You are an expert aptitude test creator. " +
                        "Return valid JSON only. Be mathematically accurate."
            ),
            GroqMessage(role = "user", content = prompt)
        )

        val response = GroqApiClient.executeChatCompletionWithFallback(
            messages = messages,
            temperature = 0.3
        )

        if (!response.isSuccessful) {
            val body = response.errorBody()?.string().orEmpty()
            throw Exception("Groq API error: HTTP ${response.code()} - $body")
        }

        val content = response.body()
            ?.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?: throw Exception("Groq returned an empty response.")

        return parseQuestions(content)
    }

    private fun parseQuestions(content: String): List<Question> {

        val cleaned = content
            .trim()
            .replace(Regex("^```(?:json)?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*```$"), "")
            .trim()

        val start = cleaned.indexOf("{")
        val end = cleaned.lastIndexOf("}")

        if (start < 0 || end <= start) {
            throw Exception("Groq returned invalid JSON.")
        }

        val array = JSONObject(cleaned.substring(start, end + 1))
            .optJSONArray("questions")
            ?: throw Exception("Questions array is missing.")

        val result = mutableListOf<Question>()

        for (i in 0 until array.length()) {

            val item = array.optJSONObject(i) ?: continue
            val text = item.optString("question").trim()
            val optionsJson = item.optJSONArray("options") ?: continue
            val correct = item.optInt("correctAnswer", -1)

            if (text.isEmpty() || optionsJson.length() != 4 || correct !in 0..3) {
                continue
            }

            val options = List(4) { optionsJson.optString(it).trim() }
            val correctText = options[correct]

            // Shuffle so the correct answer is not always in the same slot.
            val shuffled = options.shuffled()

            result.add(
                Question(
                    question = text,
                    options = shuffled,
                    correctAnswer = shuffled.indexOf(correctText)
                )
            )
        }

        if (result.size < 5) {
            throw Exception("AI returned too few valid questions. Please retry.")
        }

        return result.take(questionCount)
    }

    private fun displayQuestion() {

        if (currentQuestionIndex >= questions.size) {
            showResult()
            return
        }

        val question = questions[currentQuestionIndex]

        selectedAnswer = -1
        answered = false

        tvQuestion.text = question.question

        tvQuestionNumber.text =
            "Question ${currentQuestionIndex + 1} of ${questions.size}"

        tvScore.text = "Score: $score"

        progressBar.max = questions.size
        progressBar.progress = currentQuestionIndex + 1

        optionTexts.forEachIndexed { index, textView ->
            textView.text = question.options[index]
        }

        resetOptionStyles()

        btnNext.text = if (
            currentQuestionIndex == questions.size - 1
        ) {
            "Submit Quiz"
        } else {
            "Next Question"
        }
    }

    private fun selectAnswer(index: Int) {

        if (answered || quizFinished) return

        selectedAnswer = index

        resetOptionStyles()

        optionCards[index].setCardBackgroundColor(lightBlue)
        optionCards[index].strokeColor = blue
    }

    private fun handleNextQuestion() {

        if (selectedAnswer == -1) {
            Toast.makeText(
                this,
                "Please select an answer first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (answered || quizFinished) return

        answered = true

        val correctAnswer =
            questions[currentQuestionIndex].correctAnswer

        if (selectedAnswer == correctAnswer) {
            score++
        }

        showAnswerFeedback(correctAnswer)

        btnNext.postDelayed({

            if (quizFinished || isFinishing) return@postDelayed

            currentQuestionIndex++

            if (currentQuestionIndex < questions.size) {
                displayQuestion()
            } else {
                showResult()
            }

        }, 700)
    }

    private fun showAnswerFeedback(correctAnswer: Int) {

        optionCards.forEachIndexed { index, card ->

            when (index) {

                correctAnswer -> {
                    card.setCardBackgroundColor(lightGreen)
                    card.strokeColor = green
                }

                selectedAnswer -> {
                    card.setCardBackgroundColor(lightRed)
                    card.strokeColor = red
                }

                else -> {
                    card.setCardBackgroundColor(white)
                    card.strokeColor = grey
                }
            }
        }

        tvScore.text = "Score: $score"
    }

    private fun resetOptionStyles() {

        optionCards.forEach { card ->
            card.setCardBackgroundColor(white)
            card.strokeColor = grey
        }
    }

    // Easy: 45 seconds, Medium: 60 seconds,
    // Hard: 90 seconds per question.
    private fun startTimer() {

        val secondsPerQuestion = when (difficulty) {
            "Easy" -> 45L
            "Medium" -> 60L
            else -> 90L
        }

        val totalTime = questionCount * secondsPerQuestion * 1000L

        timer = object : CountDownTimer(totalTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000

                tvTimer.text = String.format(
                    "%02d:%02d",
                    minutes,
                    seconds
                )
            }

            override fun onFinish() {

                tvTimer.text = "00:00"
                showResult()
            }

        }.start()
    }

    private fun showResult() {

        if (quizFinished) return

        quizFinished = true
        timer?.cancel()

        val total = questions.size

        val intent = android.content.Intent(this, AptitudeResultActivity::class.java).apply {
            putExtra("topic", topic)
            putExtra("difficulty", difficulty)
            putExtra("questionCount", total)
            putExtra("score", score)
        }
        startActivity(intent)
        finish()
    }

    private fun showExitConfirmation() {

        AlertDialog.Builder(this)
            .setTitle("Exit Quiz?")
            .setMessage("Your current progress will be lost.")
            .setNegativeButton("Continue Quiz", null)
            .setPositiveButton("Exit") { _, _ ->
                timer?.cancel()
                finish()
            }
            .show()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}