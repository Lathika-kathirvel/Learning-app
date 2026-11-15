
package com.example.learningapp

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize dashboard views
        val scrollView =
            findViewById<ScrollView>(R.id.dashboardScrollView)

        val welcome =
            findViewById<TextView>(R.id.tvWelcome)

        val profileCard =
            findViewById<MaterialCardView>(R.id.cardProfile)

        val startInterviewButton =
            findViewById<MaterialButton>(R.id.btnStartInterview)

        val technicalCard =
            findViewById<MaterialCardView>(R.id.cardTechnical)

        val hrCard =
            findViewById<MaterialCardView>(R.id.cardHR)

        val aptitudeCard =
            findViewById<MaterialCardView>(R.id.cardAptitude)

        // SYSTEM BAR SPACING
        val originalLeft = scrollView.paddingLeft
        val originalRight = scrollView.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                originalLeft,
                systemBars.top + dp(30),
                originalRight,
                systemBars.bottom + dp(20)
            )

            insets
        }

        ViewCompat.requestApplyInsets(scrollView)

        // OPEN PROFILE
        profileCard.setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        }

        // START MOCK INTERVIEW - MIXED QUESTIONS
        startInterviewButton.setOnClickListener {
            openInterviewPreparation("Mixed Questions")
        }

        // TECHNICAL INTERVIEW - OPEN NEW SETUP SCREEN
        technicalCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    TechnicalInterviewSetupActivity::class.java
                )
            )
        }

        // HR INTERVIEW
        hrCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    HRInterviewActivity::class.java
                )
            )
        }

        // APTITUDE PRACTICE
        aptitudeCard.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AptitudeSetupActivity::class.java
                )
            )
        }

        // LOAD LOGGED-IN USER'S NAME
        val currentUser = auth.currentUser

        if (currentUser == null) {
            finish()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->

                val name = document.getString("name") ?: "User"

                welcome.text = "Welcome back, $name!"
            }
            .addOnFailureListener {
                welcome.text = "Welcome back!"
            }
    }

    // OPEN EXISTING INTERVIEW PREPARATION SCREEN
    private fun openInterviewPreparation(questionType: String) {

        val intent = Intent(
            this,
            InterviewPreparationActivity::class.java
        )

        intent.putExtra("questionType", questionType)

        startActivity(intent)
    }

    // DP CONVERSION
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
