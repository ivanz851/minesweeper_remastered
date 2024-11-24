package com.ivanz851.minesweeper.helpers

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import com.ivanz851.minesweeper.R

class DifficultyDialog(private val context: Context) {
    private var selectedDifficulty = 0

    fun show(onDifficultySelectedListener: (Int) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_difficulty_levels, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDifficulty = when (checkedId) {
                R.id.radioNewbie -> 0
                R.id.radioEasy -> 1
                R.id.radioMedium -> 2
                R.id.radioHard -> 3
                R.id.radioInsane -> 4
                else -> 0
            }
        }

        val alertDialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.buttonOk).setOnClickListener {
            onDifficultySelectedListener(selectedDifficulty)
            alertDialogBuilder.dismiss()
        }

        alertDialogBuilder.show()
    }
}
