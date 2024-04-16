package com.ivanz851.minesweeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.ivanz851.minesweeper.MainActivity.Companion.EXTRA_NAME
import com.ivanz851.minesweeper.databinding.ActivityGoogleSignInBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class GoogleSignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoogleSignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textDisplayName.text = intent.getStringExtra(EXTRA_NAME)
        binding.logout228.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.back.setOnClickListener {
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}