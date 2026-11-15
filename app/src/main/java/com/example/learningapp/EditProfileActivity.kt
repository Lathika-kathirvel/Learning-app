
package com.example.learningapp

import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var scrollView: ScrollView

    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: EditText
    private lateinit var skillsInput: TextInputEditText
    private lateinit var experienceLevelInput: AutoCompleteTextView
    private lateinit var educationInput: TextInputEditText
    private lateinit var targetRoleInput: TextInputEditText
    private lateinit var saveButton: MaterialButton

    private val experienceLevels = arrayOf(
        "Fresher",
        "Internship Experience",
        "Entry-level",
        "Experienced Professional"
    )

    private val textColor = Color.parseColor("#17243B")
    private val hintColor = Color.parseColor("#8993A3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        scrollView = findViewById(R.id.editProfileScrollView)

        nameInput = findViewById(R.id.etEditName)
        emailInput = findViewById(R.id.etEditEmail)
        skillsInput = findViewById(R.id.etSkills)
        experienceLevelInput = findViewById(R.id.actExperienceLevel)
        educationInput = findViewById(R.id.etEducation)
        targetRoleInput = findViewById(R.id.etTargetRole)
        saveButton = findViewById(R.id.btnSaveProfile)

        // Set input and hint text colors
        setupInputTextColors()

        // Back button
        findViewById<TextView>(R.id.btnEditBack).setOnClickListener {
            finish()
        }

        setupKeyboardHandling()
        setupAutomaticScrolling()
        setupExperienceDropdown()

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "Please log in again",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        emailInput.setText(user.email ?: "")

        loadProfile(user.uid)

        saveButton.setOnClickListener {
            saveProfile(user.uid)
        }
    }

    // INPUT TEXT AND HINT COLORS
    private fun setupInputTextColors() {

        nameInput.setTextColor(textColor)
        skillsInput.setTextColor(textColor)
        experienceLevelInput.setTextColor(textColor)
        educationInput.setTextColor(textColor)
        targetRoleInput.setTextColor(textColor)
        emailInput.setTextColor(textColor)

        nameInput.setHintTextColor(hintColor)
        skillsInput.setHintTextColor(hintColor)
        experienceLevelInput.setHintTextColor(hintColor)
        educationInput.setHintTextColor(hintColor)
        targetRoleInput.setHintTextColor(hintColor)
        emailInput.setHintTextColor(hintColor)
    }

    // KEYBOARD AND SYSTEM BAR HANDLING
    private fun setupKeyboardHandling() {

        val initialLeft = scrollView.paddingLeft
        val initialRight = scrollView.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val ime = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )

            val bottomPadding = maxOf(
                systemBars.bottom,
                ime.bottom
            )

            view.setPadding(
                initialLeft,
                systemBars.top,
                initialRight,
                bottomPadding
            )

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {

                view.postDelayed({

                    val focusedView = currentFocus

                    if (
                        focusedView != null &&
                        focusedView != scrollView &&
                        focusedView.hasFocus()
                    ) {
                        scrollFocusedField(focusedView)
                    }

                }, 200)
            }

            insets
        }

        ViewCompat.requestApplyInsets(scrollView)
    }

    // AUTOMATIC SCROLLING
    private fun setupAutomaticScrolling() {

        val fields = listOf(
            nameInput,
            skillsInput,
            experienceLevelInput,
            educationInput,
            targetRoleInput
        )

        fields.forEach { field ->

            field.setOnFocusChangeListener { view, hasFocus ->

                if (hasFocus) {

                    view.postDelayed({

                        if (view.hasFocus()) {
                            scrollFocusedField(view)
                        }

                    }, 250)
                }
            }
        }
    }

    // SCROLL FOCUSED FIELD INTO VIEW
    private fun scrollFocusedField(field: View) {

        if (!field.hasFocus()) return

        if (field.width == 0 || field.height == 0) return

        scrollView.post {

            if (!field.hasFocus()) return@post

            val rect = Rect(
                0,
                0,
                field.width,
                field.height + dp(32)
            )

            field.requestRectangleOnScreen(rect, true)
        }
    }

    // EXPERIENCE LEVEL DROPDOWN
    private fun setupExperienceDropdown() {

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.item_experience_dropdown,
            R.id.text1,
            experienceLevels
        ) {

            private fun styleItem(view: View): View {

                val textView = view as TextView

                textView.setTextColor(textColor)
                textView.textSize = 16f
                textView.typeface = Typeface.DEFAULT
                textView.setBackgroundColor(Color.WHITE)

                textView.setPadding(
                    dp(16),
                    dp(12),
                    dp(16),
                    dp(12)
                )

                return textView
            }

            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {

                return styleItem(
                    super.getView(position, convertView, parent)
                )
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {

                return styleItem(
                    super.getDropDownView(position, convertView, parent)
                )
            }
        }

        experienceLevelInput.setAdapter(adapter)

        experienceLevelInput.setTextColor(textColor)
        experienceLevelInput.setHintTextColor(hintColor)

        experienceLevelInput.setDropDownBackgroundDrawable(
            ColorDrawable(Color.WHITE)
        )

        experienceLevelInput.dropDownHeight = dp(240)

        experienceLevelInput.setOnClickListener {
            experienceLevelInput.showDropDown()
        }
    }

    // LOAD PROFILE FROM FIRESTORE
    private fun loadProfile(uid: String) {

        saveButton.isEnabled = false

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                nameInput.setText(
                    document.getString("name") ?: ""
                )

                val skills = document.get("skills") as? List<*>

                skillsInput.setText(
                    skills
                        ?.filterIsInstance<String>()
                        ?.joinToString(", ") ?: ""
                )

                experienceLevelInput.setText(
                    document.getString("experienceLevel") ?: "",
                    false
                )

                educationInput.setText(
                    document.getString("education") ?: ""
                )

                targetRoleInput.setText(
                    document.getString("targetRole") ?: ""
                )

                saveButton.isEnabled = true
            }
            .addOnFailureListener { error ->

                saveButton.isEnabled = true

                Toast.makeText(
                    this,
                    "Could not load profile: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // SAVE PROFILE TO FIRESTORE
    private fun saveProfile(uid: String) {

        val name = nameInput.text.toString().trim()
        val skillsText = skillsInput.text.toString().trim()

        val experienceLevel =
            experienceLevelInput.text.toString().trim()

        val education = educationInput.text.toString().trim()
        val targetRole = targetRoleInput.text.toString().trim()

        // Validate name
        if (name.length < 2) {

            nameInput.error = "Enter a valid name"
            nameInput.requestFocus()

            nameInput.postDelayed({
                scrollFocusedField(nameInput)
            }, 200)

            return
        }

        // Validate experience level
        if (
            experienceLevel.isNotEmpty() &&
            experienceLevel !in experienceLevels
        ) {

            experienceLevelInput.error =
                "Select a valid experience level"

            experienceLevelInput.requestFocus()
            return
        }

        // Convert skills into a list
        val skills = skillsText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        // Profile data
        val profileData = hashMapOf(
            "name" to name,
            "skills" to skills,
            "experienceLevel" to experienceLevel,
            "education" to education,
            "targetRole" to targetRole
        )

        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        db.collection("users")
            .document(uid)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Profile saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { error ->

                saveButton.isEnabled = true
                saveButton.text = "Save Profile"

                Toast.makeText(
                    this,
                    "Save failed: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // DP CONVERSION
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
