package com.fitforge.app.ui.splash

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.fitforge.app.MainActivity
import com.fitforge.app.databinding.ActivitySplashBinding
import com.fitforge.app.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivLogo.scaleX = 0.6f
        binding.ivLogo.scaleY = 0.6f

        val scaleDown = PropertyValuesHolder.ofFloat("scaleX", 1f)
        val scaleDownY = PropertyValuesHolder.ofFloat("scaleY", 1f)
        val alpha = PropertyValuesHolder.ofFloat("alpha", 1f)
        
        ObjectAnimator.ofPropertyValuesHolder(binding.ivLogo, scaleDown, scaleDownY, alpha).apply {
            duration = 500
            interpolator = OvershootInterpolator()
            start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val drawable = binding.ivLogo.drawable
            if (drawable is AnimatedVectorDrawable) {
                drawable.start()
            }
        }, 500)

        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(binding.tvTagline, "alpha", 0f, 1f).apply {
                duration = 400
                start()
            }
        }, 1200)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2200)
    }
}
