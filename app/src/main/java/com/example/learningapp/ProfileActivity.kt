
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
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Account details
    private lateinit var profileName: TextView
    private lateinit var profileInitial: TextView
    private lateinit var accountName: TextView
    private lateinit var accountEmail: TextView
    private lateinit var accountId: TextView

    // Professional details
    private lateinit var profileSkills: TextView
    private lateinit var profileExperienceLevel: TextView
    private lateinit var profileEducation: TextView
    private lateinit var profileTargetRole: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize all views
        initializeViews()

        // Handle screen insets
        setupWindowInsets()

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Edit Profile button
        findViewById<MaterialButton>(R.id.btnEditProfile)
            .setOnClickListener {

                if (auth.currentUser == null) {
                    Toast.makeText(
                        this,
                        "Please log in again",
                        Toast.LENGTH_SHORT
                    ).show()

                    openLogin()
                    return@setOnClickListener
                }

                val intent = Intent(
                    this,
                    EditProfileActivity::class.java
                )

                startActivity(intent)
            }

        // Logout button
        findViewById<MaterialButton>(R.id.btnLogout)
            .setOnClickListener {

                auth.signOut()

                Toast.makeText(
                    this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
                ).show()

                openLogin()
            }
    }

    // INITIALIZE VIEWS
    private fun initializeViews() {

        // Account details
        profileName = findViewById(R.id.tvProfileName)
        profileInitial = findViewById(R.id.tvProfileInitial)
        accountName = findViewById(R.id.tvAccountName)
        accountEmail = findViewById(R.id.tvAccountEmail)
        accountId = findViewById(R.id.tvAccountId)

        // Professional details
        profileSkills = findViewById(R.id.tvProfileSkills)

        profileExperienceLevel =
            findViewById(R.id.tvProfileExperienceLevel)

        profileEducation =
            findViewById(R.id.tvProfileEducation)

        profileTargetRole =
            findViewById(R.id.tvProfileTargetRole)
    }

    // WINDOW INSETS
    private fun setupWindowInsets() {

        val scrollView = findViewById<ScrollView>(
            R.id.profileScrollView
        )

        val originalLeft = scrollView.paddingLeft
        val originalRight = scrollView.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(
            scrollView
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val density = resources.displayMetrics.density

            view.setPadding(
                originalLeft,
                systemBars.top + (10 * density).toInt(),
                originalRight,
                systemBars.bottom + (20 * density).toInt()
            )

            insets
        }

        ViewCompat.requestApplyInsets(scrollView)
    }

    // RELOAD PROFILE WHEN SCREEN BECOMES VISIBLE
    override fun onResume() {
        super.onResume()

        if (::auth.isInitialized && ::db.isInitialized) {
            loadProfile()
        }
    }

    // LOAD PROFILE FROM FIRESTORE
    private fun loadProfile() {

        val user = auth.currentUser

        if (user == null) {
            openLogin()
            return
        }

        // Show loading information
        profileName.text = "Loading..."
        accountName.text = "Loading..."
        profileSkills.text = "Loading..."
        profileExperienceLevel.text = "Loading..."
        profileEducation.text = "Loading..."
        profileTargetRole.text = "Loading..."

        // Firebase Authentication details
        accountEmail.text = user.email ?: "Not available"
        accountId.text = user.uid

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (isFinishing || isDestroyed) {
                    return@addOnSuccessListener
                }

                if (!document.exists()) {

                    profileName.text = "User"
                    accountName.text = "User"
                    profileInitial.text = "U"

                    profileSkills.text = "Not added"
                    profileExperienceLevel.text = "Not added"
                    profileEducation.text = "Not added"
                    profileTargetRole.text = "Not added"

                    Toast.makeText(
                        this,
                        "Profile information not found",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener
                }

                // NAME
                val name = document.getString("name")
                    ?.takeIf { it.isNotBlank() }
                    ?: "User"

                profileName.text = name
                accountName.text = name

                profileInitial.text = name
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString() ?: "U"

                // SKILLS
                val skills = document.get("skills") as? List<*>

                profileSkills.text = skills
                    ?.filterIsInstance<String>()
                    ?.joinToString(", ")
                    ?.takeIf { it.isNotBlank() }
                    ?: "Not added"

                // EXPERIENCE LEVEL
                profileExperienceLevel.text =
                    document.getString("experienceLevel")
                        ?.takeIf { it.isNotBlank() }
                        ?: "Not added"

                // EDUCATION
                profileEducation.text =
                    document.getString("education")
                        ?.takeIf { it.isNotBlank() }
                        ?: "Not added"

                // TARGET ROLE
                profileTargetRole.text =
                    document.getString("targetRole")
                        ?.takeIf { it.isNotBlank() }
                        ?: "Not added"
            }
            .addOnFailureListener { error ->

                if (isFinishing || isDestroyed) {
                    return@addOnFailureListener
                }

                Toast.makeText(
                    this,
                    "Unable to load profile: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // NAVIGATE TO LOGIN
    private fun openLogin() {

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
