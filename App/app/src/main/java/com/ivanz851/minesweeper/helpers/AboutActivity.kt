package com.ivanz851.minesweeper.helpers

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ivanz851.minesweeper.MainActivity
import com.ivanz851.minesweeper.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }


    fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btn_to_main_menu -> {
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}