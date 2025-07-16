package com.app.hydraware

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logoImageView)
        val animator = ObjectAnimator.ofFloat(logo, "rotation", 0f, 360f)
        animator.duration = 1200
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.repeatCount = 1
        animator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, AnalysisActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1800)
    }
} 