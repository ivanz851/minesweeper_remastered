package com.ivanz851.minesweeper

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ivanz851.minesweeper.databinding.ActivityMainBinding
import com.ivanz851.minesweeper.helpers.AboutActivity
import com.ivanz851.minesweeper.models.User
import com.rengwuxian.materialedittext.MaterialEditText
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    private val tag: String = MainActivity::class.java.simpleName

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupViews()
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupViews() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1024959107236-1h421a50djv6doa8o0j9ua901grhmp04.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.vkBtn.setOnClickListener {
            signOutEverything()

            VKSdk.login(this)
        }

        binding.btnSignInGoogle.setOnClickListener {
            signOutEverything()
            signInViaGoogle()
        }

        binding.btnSignUp.setOnClickListener {
            signOutEverything()
            showRegisterWindow()
        }

        binding.btnSignIn.setOnClickListener {
            signOutEverything()
            FirebaseAuth.getInstance().signOut()
            showSignInWindow()
        }

        binding.mainBtnStart.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java);

            startActivity(intent)
            finish()
        }

        binding.mainBtnAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.mainBtnExit.setOnClickListener {
            signOutEverything()
            finish()
        }
    }

    private fun signOutEverything() {
        Firebase.auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1024959107236-1h421a50djv6doa8o0j9ua901grhmp04.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        FirebaseAuth.getInstance().signOut()
        VKSdk.logout()
    }


    private fun signInViaGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object :
                VKCallback<VKAccessToken> {
                override fun onResult(res: VKAccessToken) {
                    firebaseAuthWithVk(res.accessToken, res.userId)
                }
                override fun onError(error: VKError) {
                }
            })) {
            super.onActivityResult(requestCode, resultCode, data)
        }


        if (requestCode == GOOGLE_RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                val errorMessage = "Google Sign In Exception: ${e.message}"
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            }
        }
    }


    private fun firebaseAuthWithVk(idToken: String, userId: String) {
        val email = "vkauthorized-+$userId@vkauth.ru"
        val password = "vkauth-+$userId-password"

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "SIGN IN SUCCESSFUL", Snackbar.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val user = User()
                        user.setEmail(email)
                        user.setName("vk_user")
                        user.setPassword(password)
                        user.setPhone("00000000000")

                        Snackbar.make(binding.root, "SIGN IN SUCCESSFUL", Snackbar.LENGTH_LONG).show()

                        FirebaseAuth.getInstance().currentUser?.let {
                            FirebaseDatabase.getInstance().getReference("Users").child(it.uid)
                                .setValue(user)
                        }
                    }.addOnFailureListener {
                    }
            }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Snackbar.make(binding.root, "SIGN IN SUCCESSFUL", Snackbar.LENGTH_LONG).show()

                    val user = FirebaseAuth.getInstance().currentUser

                    user?.let {
                        val name = user.displayName
                        val email = user.email+ "@googleauth.com"
                        val userId = user.uid

                        FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    val newUser = User(
                                        name ?: "",
                                        email, userId, ""
                                    )

                                    FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                                        .setValue(newUser)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                } else {
                    Snackbar.make(binding.root, "GOOGLE SIGN IN ERROR!", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    companion object {
        const val GOOGLE_RC_SIGN_IN = 1001
        const val VK_RC_SIGN_IN = 1002
        const val EXTRA_NAME = "EXTRA_NAME"
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
                Snackbar.make(binding.root, "Enter your email", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }


            if (!isEmailValid(email.text.toString())) {
                Snackbar.make(binding.root, "Enter correct e-mail", Snackbar.LENGTH_LONG)
                    .show()
                return@setPositiveButton
            }
            if (!isPasswordValid(password.text.toString())) {
                Snackbar.make(binding.root, "Enter correct password", Snackbar.LENGTH_LONG)
                    .show()
                return@setPositiveButton
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnSuccessListener {
                    Snackbar.make(binding.root, "SIGN IN SUCCESSFUL", Snackbar.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(binding.root, "Auth error! ${e.message}", Snackbar.LENGTH_LONG).show()
                }
        }

        dialog.show()
    }

    private fun showRegisterWindow() {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
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
                Snackbar.make(binding.root, "Enter your email", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }
            if (TextUtils.isEmpty(name.text.toString())) {
                Snackbar.make(binding.root, "Enter your name", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }
            if (TextUtils.isEmpty(phone.text.toString())) {
                Snackbar.make(binding.root, "Enter your phone", Snackbar.LENGTH_LONG).show()
                return@setPositiveButton
            }

            if (!isEmailValid(email.text.toString())) {
                Snackbar.make(binding.root, "Enter correct e-mail", Snackbar.LENGTH_LONG)
                    .show()
                return@setPositiveButton
            }
            if (!isPhoneNumberValid(phone.text.toString())) {
                Snackbar.make(binding.root, "Enter correct phone number", Snackbar.LENGTH_LONG)
                    .show()
                return@setPositiveButton
            }
            if (!isPasswordValid(password.text.toString())) {
                Snackbar.make(binding.root, "Enter reliable password", Snackbar.LENGTH_LONG)
                    .show()
                return@setPositiveButton
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnSuccessListener {
                    val user = User()
                    user.setEmail(email.text.toString())
                    user.setName(name.text.toString())
                    user.setPassword(password.text.toString())
                    user.setPhone(phone.text.toString())

                    FirebaseAuth.getInstance().currentUser?.let {
                        FirebaseDatabase.getInstance().getReference("Users").child(it.uid)
                            .setValue(user)
                            .addOnSuccessListener {
                                Snackbar.make(
                                    binding.root,
                                    "User successfully added!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Snackbar.make(binding.root, "Registration ERROR! ${e.message}", Snackbar.LENGTH_LONG)
                        .show()
                }

        }
        dialog.show()
    }


    fun isEmailValid(emailAddress : String) : Boolean {
        val pattern : Pattern = Pattern.compile(
            ("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                    + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"))
        return pattern.matcher(emailAddress).matches()
    }

    fun isPhoneNumberValid(phoneNumber : String): Boolean {
        val pattern : Pattern = Pattern.compile((
                "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
                        + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
                        + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$"))
        return pattern.matcher(phoneNumber).matches()
    }

    fun isPasswordValid(password : String) : Boolean {
        val pattern : Pattern = Pattern.compile(
            ("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$"))
        return pattern.matcher(password).matches()
    }
}