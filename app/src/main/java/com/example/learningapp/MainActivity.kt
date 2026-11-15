
package com.example.learningapp

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Patterns
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

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val scrollView = findViewById<ScrollView>(R.id.loginScrollView)
        val email = findViewById<TextInputEditText>(R.id.etEmail)
        val password = findViewById<TextInputEditText>(R.id.etPassword)
        val loginButton = findViewById<MaterialButton>(R.id.btnLogin)
        val signUp = findViewById<TextView>(R.id.tvSignUp)

        // Keyboard handling
        val originalLeft = scrollView.paddingLeft
        val originalRight = scrollView.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val imeInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )

            view.setPadding(
                originalLeft,
                systemBars.top,
                originalRight,
                maxOf(systemBars.bottom, imeInsets.bottom)
            )

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                view.post {
                    currentFocus?.let { focusedView ->
                        focusedView.requestRectangleOnScreen(
                            Rect(
                                0,
                                0,
                                focusedView.width,
                                focusedView.height
                            ),
                            true
                        )
                    }
                }
            }

            insets
        }

        ViewCompat.requestApplyInsets(scrollView)

        listOf(email, password).forEach { input ->

            input.setOnFocusChangeListener { view, hasFocus ->

                if (hasFocus) {
                    view.postDelayed({
                        view.requestRectangleOnScreen(
                            Rect(
                                0,
                                0,
                                view.width,
                                view.height
                            ),
                            true
                        )
                    }, 250)
                }
            }
        }

        // Login validation and Firebase authentication
        loginButton.setOnClickListener {

            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString()

            when {

                userEmail.isEmpty() -> {
                    email.error = "Enter your email"
                    email.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() -> {
                    email.error = "Enter a valid email"
                    email.requestFocus()
                }

                userPassword.isEmpty() -> {
                    password.error = "Enter your password"
                    password.requestFocus()
                }

                else -> {

                    loginButton.isEnabled = false
                    loginButton.text = "Logging in..."

                    auth.signInWithEmailAndPassword(
                        userEmail,
                        userPassword
                    ).addOnCompleteListener { task ->

                        loginButton.isEnabled = true
                        loginButton.text = "Log In"

                        if (task.isSuccessful) {

                            Toast.makeText(
                                this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this,
                                    DashboardActivity::class.java
                                )
                            )

                            finish()

                        } else {

                            Toast.makeText(
                                this,
                                task.exception?.localizedMessage
                                    ?: "Login failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        // Navigate to Signup
        signUp.setOnClickListener {

            startActivity(
                Intent(this, SignUpActivity::class.java)
            )
        }
    }

}
