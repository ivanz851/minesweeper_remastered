package com.ivanz851.minesweeper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class GameActivity : AppCompatActivity(), MineSweeperView.OnScoreChangeListener, MineSweeperView.OnGameEndListener {
    private lateinit var mineSweeperView: MineSweeperView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var highScoreTextView: TextView

    private var elapsedTime = 0
    private var isTimerRunning = false
    private val timerHandler = Handler()
    private var highScore = 0

    private val timerRunnable = object : Runnable {
        override fun run() {
            elapsedTime++
            timerTextView.text = String.format(getString(R.string.time), elapsedTime)
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        highScoreTextView = findViewById(R.id.tvHighScore)

        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        highScore = sharedPrefs.getInt("highScore", 0)

        highScoreTextView.text = String.format(getString(R.string.high_score_text), highScore)
        mineSweeperView = findViewById(R.id.mineSweeperView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        scoreTextView = findViewById(R.id.tvScore)
        scoreTextView.text = getString(R.string.score, 0)
        timerTextView = findViewById(R.id.tvTimer)

        swipeRefreshLayout.setOnRefreshListener {
            resetGame()
            swipeRefreshLayout.isRefreshing = false
        }

        mineSweeperView.setOnScoreChangeListener(this)

        if (!isTimerRunning) {
            timerHandler.post(timerRunnable)
            isTimerRunning = true
        }

        mineSweeperView.setOnGameEndListener(this)
    }

    private fun resetGame() {
        mineSweeperView.resetGame()
        mineSweeperView.invalidate()
        elapsedTime = 0
        timerTextView.text = String.format(getString(R.string.time), elapsedTime)
        if (!isTimerRunning) {
            timerHandler.post(timerRunnable)
            isTimerRunning = true
        }
    }

    override fun onScoreChanged(score: Int) {
        scoreTextView.text = getString(R.string.score, score)

    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(timerRunnable)
    }

    override fun onGameEnd() {
        isTimerRunning = false
        timerHandler.removeCallbacks(timerRunnable)

        val score = mineSweeperView.getCurrentScore()
        if (score > highScore) {
            highScore = score
            val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putInt("highScore", highScore).apply()

            highScoreTextView.text = String.format(getString(R.string.high_score_text), highScore)

        }
    }

    fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btn_to_main_menu -> {
                onGameEnd()
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
