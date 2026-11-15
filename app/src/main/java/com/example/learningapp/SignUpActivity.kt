
package com.example.learningapp

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.WindowManager
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

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Keyboard configuration
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // Handle system bars and keyboard insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Connect XML views
        val name = findViewById<TextInputEditText>(R.id.etName)
        val email = findViewById<TextInputEditText>(R.id.etEmail)
        val password = findViewById<TextInputEditText>(R.id.etPassword)
        val confirmPassword =
            findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val createAccount =
            findViewById<MaterialButton>(R.id.btnCreateAccount)

        val login = findViewById<TextView>(R.id.tvLogin)

        val scrollView =
            findViewById<ScrollView>(R.id.signupScrollView)

        // Adjust the visible ScrollView area for the keyboard
        val initialLeft = scrollView.paddingLeft
        val initialRight = scrollView.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val keyboardInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )

            val keyboardVisible = insets.isVisible(
                WindowInsetsCompat.Type.ime()
            )

            view.setPadding(
                initialLeft,
                systemBars.top,
                initialRight,
                maxOf(systemBars.bottom, keyboardInsets.bottom)
            )

            // Keep the focused input visible
            if (keyboardVisible) {
                view.post {
                    currentFocus?.let { focusedView ->
                        val rect = Rect(
                            0,
                            0,
                            focusedView.width,
                            focusedView.height
                        )

                        focusedView.requestRectangleOnScreen(
                            rect,
                            false
                        )
                    }
                }
            }

            insets
        }

        ViewCompat.requestApplyInsets(scrollView)

        // Scroll focused fields into view
        val inputFields = listOf(
            name,
            email,
            password,
            confirmPassword
        )

        inputFields.forEach { input ->

            input.setOnFocusChangeListener { view, hasFocus ->

                if (hasFocus) {
                    view.postDelayed({

                        val rect = Rect(
                            0,
                            0,
                            view.width,
                            view.height
                        )

                        view.requestRectangleOnScreen(
                            rect,
                            false
                        )

                    }, 250)
                }
            }
        }

        // Registration button
        createAccount.setOnClickListener {

            val userName = name.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString()
            val confirm = confirmPassword.text.toString()

            // Name validation
            if (userName.isEmpty()) {
                name.error = "Enter your full name"
                name.requestFocus()
                return@setOnClickListener
            }

            // Email validation
            if (userEmail.isEmpty()) {
                email.error = "Enter your email"
                email.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(userEmail).matches()
            ) {
                email.error = "Enter a valid email"
                email.requestFocus()
                return@setOnClickListener
            }

            // Password validation
            if (userPassword.length < 6) {
                password.error =
                    "Password must contain at least 6 characters"
                password.requestFocus()
                return@setOnClickListener
            }

            // Confirm password validation
            if (confirm.isEmpty()) {
                confirmPassword.error = "Confirm your password"
                confirmPassword.requestFocus()
                return@setOnClickListener
            }

            if (userPassword != confirm) {
                confirmPassword.error = "Passwords do not match"
                confirmPassword.requestFocus()
                return@setOnClickListener
            }

            createAccount.isEnabled = false
            createAccount.text = "Creating account..."

            // Firebase Authentication
            auth.createUserWithEmailAndPassword(
                userEmail,
                userPassword
            ).addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val firebaseUser = auth.currentUser

                    if (firebaseUser == null) {

                        createAccount.isEnabled = true
                        createAccount.text = "Create Account"

                        Toast.makeText(
                            this,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addOnCompleteListener
                    }

                    // User profile data
                    val userData = hashMapOf(
                        "uid" to firebaseUser.uid,
                        "name" to userName,
                        "email" to userEmail,
                        "createdAt" to System.currentTimeMillis()
                    )

                    // Save profile in Firestore
                    db.collection("users")
                        .document(firebaseUser.uid)
                        .set(userData)
                        .addOnSuccessListener {

                            Toast.makeText(
                                this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                            auth.signOut()

                            startActivity(
                                Intent(this, MainActivity::class.java)
                            )

                            finish()
                        }
                        .addOnFailureListener { exception ->

                            // Delete Auth account if profile save fails
                            firebaseUser.delete()
                                .addOnCompleteListener {

                                    createAccount.isEnabled = true
                                    createAccount.text = "Create Account"

                                    Toast.makeText(
                                        this,
                                        "Could not save profile: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                        }

                } else {

                    createAccount.isEnabled = true
                    createAccount.text = "Create Account"

                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Return to Login screen
        login.setOnClickListener {
            finish()
        }
    }
}
