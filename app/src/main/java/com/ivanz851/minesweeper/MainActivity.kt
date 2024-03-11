package com.ivanz851.minesweeper

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ivanz851.minesweeper.Models.User
import com.rengwuxian.materialedittext.MaterialEditText

class MainActivity : AppCompatActivity() {
    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var users: DatabaseReference

    private lateinit var root : RelativeLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSignIn = findViewById(R.id.btn_sign_in)
        btnSignUp = findViewById(R.id.btn_sign_up)

        root = findViewById(R.id.root_element)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        users = db.getReference("Users")

        btnSignUp.setOnClickListener {
            showRegisterWindow()
        }
        btnSignIn.setOnClickListener {
            showSignInWindow()
        }
    }

    private fun showSignInWindow() {
        val dialog : AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle("Sign in")
        dialog.setMessage("Enter your email and password")

        val inflater = LayoutInflater.from(this)
        val signInWindow: View = inflater.inflate(R.layout.sign_in_window, null)

        dialog.setView(signInWindow)

        val email: MaterialEditText = signInWindow.findViewById(R.id.emailField)
        val password: MaterialEditText = signInWindow.findViewById(R.id.passwordField)

        dialog.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        dialog.setPositiveButton("Enter") { _, _ ->
            if (TextUtils.isEmpty(email.text.toString())) {
                Snackbar.make(root, "Enter your email", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }

            if (password.text.toString().length < 8) {
                Snackbar.make(root, "Enter password longer than 8 symbols", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }

            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnSuccessListener {
                    Snackbar.make(root, "SIGN IN SUCCESSFUL", Snackbar.LENGTH_LONG).show()

                    /*
                    val intent = Intent(this, MapActivity::class.java)
                    startActivity(intent)
                    finish()
                     */
                }
                .addOnFailureListener { e ->
                    Snackbar.make(root, "Auth error! ${e.message}", Snackbar.LENGTH_LONG).show()
                }
        }

        dialog.show()
    }

    private fun showRegisterWindow() {
        val dialog : AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle("Sign up")
        dialog.setMessage("Enter your registration data")

        val inflater = LayoutInflater.from(this)
        val signUpWindow: View = inflater.inflate(R.layout.sign_up_window, null)

        dialog.setView(signUpWindow)

        val email: MaterialEditText = signUpWindow.findViewById(R.id.emailField)
        val password: MaterialEditText = signUpWindow.findViewById(R.id.passwordField)
        val name: MaterialEditText = signUpWindow.findViewById(R.id.nameField)
        val phone: MaterialEditText = signUpWindow.findViewById(R.id.phoneField)

        dialog.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        dialog.setPositiveButton("Add") { _, _ ->
            if (TextUtils.isEmpty(email.text.toString())) {
                Snackbar.make(root, "Enter your email", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }
            if (TextUtils.isEmpty(name.text.toString())) {
                Snackbar.make(root, "Enter your name", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }
            if (TextUtils.isEmpty(phone.text.toString())) {
                Snackbar.make(root, "Enter your phone", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }
            if (password.text.toString().length < 8) {
                Snackbar.make(root, "Enter password longer than 8 symbols", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }

            // Registration successful
            auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnSuccessListener {
                    val user = User()
                    user.setEmail(email.text.toString())
                    user.setName(name.text.toString())
                    user.setPassword(password.text.toString())
                    user.setPhone(phone.text.toString())

                    FirebaseAuth.getInstance().currentUser?.let {
                        users.child(it.uid)
                            .setValue(user)
                            .addOnSuccessListener {
                                Snackbar.make(root, "User successfully added!", Snackbar.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Snackbar.make(root, "Registration ERROR! ${e.message}", Snackbar.LENGTH_LONG).show()
                }

        }

        dialog.show()
    }


    fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.main_btn_start -> {
                val intent = Intent(this, GameActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.main_btn_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                finish()
            }
            //R.id.btn_sign_up -> showRegisterWindow()
            R.id.main_btn_exit -> finish()
        }
    }


}