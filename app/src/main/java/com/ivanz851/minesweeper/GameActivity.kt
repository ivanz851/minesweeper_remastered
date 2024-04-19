package com.ivanz851.minesweeper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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

class GameActivity : AppCompatActivity(), MineSweeperView.OnScoreChangeListener, MineSweeperView.OnGameEndListener {
    private lateinit var mineSweeperView: MineSweeperView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var highScoreTextView: TextView
    private lateinit var hintsTextView : TextView
    private lateinit var adView : BannerAdView

    private var elapsedTime = 0
    private var isTimerRunning = false
    private val timerHandler = Handler()
    private var highScore = 0

    private var rewardedAd: RewardedAd? = null
    private var rewardedAdLoader: RewardedAdLoader? = null
    private var hintsCount = 0

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

        // Yandex rewarded ads start
        // Rewarded ads loading should occur after initialization of the SDK.
        // Initialize SDK as early as possible, for example in Application.onCreate or Activity.onCreate
        rewardedAdLoader = RewardedAdLoader(this).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewarded: RewardedAd) {
                    rewardedAd = rewarded
                    // The ad was loaded successfully. Now you can show loaded ad.
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    // Ad failed to load with AdRequestError.
                    // Attempting to load a new ad from the onAdFailedToLoad() method is strongly discouraged.
                }
            })
        }
        loadRewardedAd()
        //Yandex rewarded ads end




        highScoreTextView = findViewById(R.id.tvHighScore)

        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        highScore = sharedPrefs.getInt("highScore", 0)

        highScoreTextView.text = String.format(getString(R.string.high_score_text), highScore)
        mineSweeperView = findViewById(R.id.mineSweeperView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        scoreTextView = findViewById(R.id.tvScore)
        hintsTextView = findViewById(R.id.tvHints)
        scoreTextView.text = getString(R.string.score, 0)
        timerTextView = findViewById(R.id.tvTimer)



        // ADS start

        MobileAds.initialize(this) {
            Log.d("MyLog", "Yandex Ads SDK initialized")
        }

        adView = findViewById(R.id.banner188)

        adView.setAdUnitId("demo-banner-yandex")
        adView.setAdSize(BannerAdSize.stickySize(this, 350))

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)



        // ADS end




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

    private fun loadRewardedAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder("demo-rewarded-yandex").build()
        rewardedAdLoader?.loadAd(adRequestConfiguration)
    }

    private fun showAd() {
        rewardedAd?.apply {
            setAdEventListener(object : RewardedAdEventListener {
                override fun onAdShown() {
                    // Called when ad is shown.
                }

                override fun onAdFailedToShow(adError: AdError) {
                    // Called when an RewardedAd failed to show

                    // Clean resources after Ad failed to show
                    rewardedAd?.setAdEventListener(null)
                    rewardedAd = null

                    // Now you can preload the next rewarded ad.
                    loadRewardedAd()
                }

                override fun onAdDismissed() {
                    // Called when ad is dismissed.
                    // Clean resources after Ad dismissed
                    rewardedAd?.setAdEventListener(null)
                    rewardedAd = null

                    // Now you can preload the next rewarded ad.
                    loadRewardedAd()
                }

                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    // Called when an impression is recorded for an ad.
                }

                override fun onRewarded(reward: Reward) {
                    // Called when the user can be rewarded.
                    GetHintAdd()
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

    private fun GetHintAdd() {
        hintsCount++;
        // TODO : binding с отображением числа монет (сначала надо переписать весь код выше при помощи binding)
         //binding.hintCountTextView.text="Hints:$hintsCount"
        hintsTextView.text = getString(R.string.hints, hintsCount)
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
            R.id.hint_btn -> {
                showAd()
            }
            R.id.settings_btn -> {
                // TODO реализовать выбор уровня сложности
            }
        }
    }
}
