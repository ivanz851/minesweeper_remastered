package com.ivanz851.minesweeper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ivanz851.minesweeper.helpers.DifficultyDialog
import com.ivanz851.minesweeper.listeners.OnGameEndListener
import com.ivanz851.minesweeper.listeners.OnHintsCountChangeListener
import com.ivanz851.minesweeper.listeners.OnScoreChangeListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader


class GameActivity : AppCompatActivity(), OnHintsCountChangeListener, OnScoreChangeListener, OnGameEndListener {
    private val tag: String = GameActivity::class.java.simpleName

    private lateinit var gameFieldView: GameFieldView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var highScoreTextView: TextView
    private lateinit var hintsTextView : TextView
    private lateinit var adView : BannerAdView
    private lateinit var hintSwitch : SwitchCompat

    private lateinit var database: DatabaseReference

    private var elapsedTime = 0
    private var isTimerRunning = false
    private val timerHandler = Handler()
    private var highScore = 0

    private var hintsCount = 0

    private var rewardedAd: RewardedAd? = null
    private var rewardedAdLoader: RewardedAdLoader? = null

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

        database = FirebaseDatabase.getInstance().getReference("Users")

        hintSwitch = findViewById(R.id.hintSwitch)

        rewardedAdLoader = RewardedAdLoader(this).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewarded: RewardedAd) {
                    rewardedAd = rewarded
                    loadStuff()
                }

                override fun onAdFailedToLoad(error: AdRequestError) {

                }
            })
        }
        loadRewardedAd()
    }

    private fun loadStuff() {
        highScoreTextView = findViewById(R.id.tvHighScore)

        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            highScore = sharedPrefs.getInt("highScore", 0)
        } else {
            database.child(user.uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val bestScoreValue = it.child("bestScore").value
                    if (bestScoreValue is Long) {
                        highScore = bestScoreValue.toInt()
                        highScoreTextView.text = String.format(getString(R.string.high_score_text), highScore)
                    }
                }
            }.addOnFailureListener {
            }
        }

        gameFieldView = findViewById(R.id.gameFieldView)
        gameFieldView.setHintSwitch(hintSwitch)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        scoreTextView = findViewById(R.id.tvScore)
        hintsTextView = findViewById(R.id.tvHints)
        scoreTextView.text = getString(R.string.score, 0)
        timerTextView = findViewById(R.id.tvTimer)
        hintsTextView.text = getString(R.string.hints, 0)

        loadAdBanner()

        gameFieldView.setOnHintsCountChangeListener(this)
        changeHintsCount(0)

        swipeRefreshLayout.setOnRefreshListener {
            resetGame()
            swipeRefreshLayout.isRefreshing = false
        }

        gameFieldView.setOnScoreChangeListener(this)

        if (!isTimerRunning) {
            timerHandler.post(timerRunnable)
            isTimerRunning = true
        }

        gameFieldView.setOnGameEndListener(this)
    }

    private fun loadAdBanner() {
        MobileAds.initialize(this) {
        }

        adView = findViewById(R.id.banner188)

        adView.setAdUnitId("demo-banner-yandex")
        adView.setAdSize(BannerAdSize.stickySize(this, 350))

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun loadRewardedAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder("demo-rewarded-yandex").build()
        rewardedAdLoader?.loadAd(adRequestConfiguration)
    }

    private fun showAd() {
        rewardedAd?.apply {
            setAdEventListener(object : RewardedAdEventListener {
                override fun onAdShown() {
                }

                override fun onAdFailedToShow(adError: AdError) {
                    rewardedAd?.setAdEventListener(null)
                    rewardedAd = null

                    loadRewardedAd()
                }

                override fun onAdDismissed() {
                    rewardedAd?.setAdEventListener(null)
                    rewardedAd = null

                    loadRewardedAd()
                }

                override fun onAdClicked() {
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                }

                override fun onRewarded(reward: Reward) {
                    changeHintsCount(1)
                }
            })
            show(this@GameActivity)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(timerRunnable)
        rewardedAdLoader?.setAdLoadListener(null)
        rewardedAdLoader = null
        destroyRewardedAd()
    }


    private fun destroyRewardedAd() {
        rewardedAd?.setAdEventListener(null)
        rewardedAd = null
    }


    private fun resetGame() {
        gameFieldView.resetGame()
        gameFieldView.invalidate()
        elapsedTime = 0
        timerTextView.text = String.format(getString(R.string.time), elapsedTime)
        if (!isTimerRunning) {
            timerHandler.post(timerRunnable)
            isTimerRunning = true
        }
    }


    override fun onHintCountChanged(count: Int) {
        changeHintsCount(-1)
    }


    override fun onScoreChanged(score: Int) {
        scoreTextView.text = getString(R.string.score, score)
    }


    override fun onGameEnd(winState: Boolean) {
        isTimerRunning = false
        timerHandler.removeCallbacks(timerRunnable)

        val score = gameFieldView.getCurrentScore()
        if (winState) {
            updHighScore(score)
        }
    }


    private fun updHighScore(score: Int) {
        if (score <= highScore) {
            return
        }

        highScore = score
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putInt("highScore", highScore).apply()
        } else {
            highScoreTextView.text = String.format(getString(R.string.high_score_text), highScore)
            saveHighScoreToFirebase(highScore)
        }
    }


    private fun saveHighScoreToFirebase(newHighScore: Int) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val currentUserRef = database.child(user.uid)

        currentUserRef.child("bestScore").setValue(newHighScore)
    }


    fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btn_to_main_menu -> {
                onGameEnd(false)
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.hint_btn -> {
                showAd()
            }
            R.id.settings_btn -> {
                val difficultyDialog = DifficultyDialog(this)
                difficultyDialog.show { selectedDifficulty ->
                    var newWidth = 5
                    var newHeight = 5
                    when (selectedDifficulty) {
                        0 -> {
                            newWidth = 5
                            newHeight = 5
                        }
                        1 -> {
                            newWidth = 4
                            newHeight = 8
                        }
                        2 -> {
                            newWidth = 6
                            newHeight = 6
                        }
                        3 -> {
                            newWidth = 6
                            newHeight = 11
                        }
                        4 -> {
                            newWidth = 12
                            newHeight = 22
                        }
                    }
                    gameFieldView.setBoardSize(newWidth, newHeight)
                    gameFieldView.onSizeChanged(newWidth, newHeight, gameFieldView.width, gameFieldView.height)

                    resetGame()
                }
            }
        }
    }


    private fun updateHintSwitchAvailability() {
        hintSwitch.isEnabled = hintsCount > 0
    }


    private fun changeHintsCount(delta: Int) {
        hintsCount += delta
        updateHintSwitchAvailability()
        hintsTextView.text = getString(R.string.hints, hintsCount)
    }
}
