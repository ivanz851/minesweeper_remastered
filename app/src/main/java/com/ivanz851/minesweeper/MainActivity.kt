package com.ivanz851.minesweeper

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.ivanz851.minesweeper.Models.User
import com.ivanz851.minesweeper.databinding.ActivityMainBinding
import com.rengwuxian.materialedittext.MaterialEditText
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import com.vk.sdk.util.VKUtil

class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.simpleName

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore : FirebaseFirestore

    private lateinit var db: FirebaseDatabase
    private lateinit var users: DatabaseReference

    private lateinit var root : RelativeLayout

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupViews()

        val fingerprints = VKUtil.getCertificateFingerprint(this, this.packageName)
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupViews() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        users = db.getReference("Users")
        root = binding.root

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1024959107236-1h421a50djv6doa8o0j9ua901grhmp04.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.vkBtn.setOnClickListener {
            VKSdk.login(this@MainActivity)
        }


        binding.btnSignInGoogle.setOnClickListener {
            signIn()
        }

        binding.btnSignUp.setOnClickListener {
            showRegisterWindow()
        }

        binding.btnSignIn.setOnClickListener {
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
            finish()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val callback = object : VKAuthCallback, VKCallback<com.vk.sdk.VKAccessToken> {
            override fun onLogin(token: VKAccessToken) {
                val userId = token.userId

                val emailVk : String = "hahaha"
                auth.signInWithEmailAndPassword(emailVk, emailVk)
                    .addOnCompleteListener { authResult ->
                        if (authResult.isSuccessful) {
                            Snackbar.make(root, "SIGN IN SUCCESSFUL", Snackbar.LENGTH_LONG).show()
                        } else {
                        }

                }
            }

            override fun onLoginFailed(error: VKAuthException) {
            }

            override fun onResult(res: com.vk.sdk.VKAccessToken?) {

            }

            override fun onError(error: VKError?) {

            }
        }
        if (data == null || !VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(applicationContext, GoogleSignInActivity::class.java)
            intent.putExtra(EXTRA_NAME, user.displayName)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val RC_SIGN_IN = 1001
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
                }
                .addOnFailureListener { e ->
                    Snackbar.make(root, "Auth error! ${e.message}", Snackbar.LENGTH_LONG).show()
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
                Snackbar.make(root, "Enter password longer than 8 symbols", Snackbar.LENGTH_LONG)
                    .show()
                return@setPositiveButton
            }

            auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnSuccessListener {
                    val user = User()
                    user.setEmail(email.text.toString())
                    user.setName(name.text.toString())
                    user.setPassword(password.text.toString())
                    user.setPhone(phone.text.toString())


                    Snackbar.make(root, "User successfully added!", Snackbar.LENGTH_LONG).show()

                    FirebaseAuth.getInstance().currentUser?.let {
                        users.child(it.uid)
                            .setValue(user)
                            .addOnSuccessListener {
                                Snackbar.make(
                                    root,
                                    "User successfully added!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Snackbar.make(root, "Registration ERROR! ${e.message}", Snackbar.LENGTH_LONG)
                        .show()
                }

        }
        dialog.show()
    }
}