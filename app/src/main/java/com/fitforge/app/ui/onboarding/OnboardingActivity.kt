package com.fitforge.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.fitforge.app.MainActivity
import com.fitforge.app.data.local.PrefsManager
import com.fitforge.app.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PrefsManager(this)

        val pages = listOf(
            OnboardingPage("No more dead streaks", "Miss a day. Lose 15%. Not everything. Your momentum builds over time, not resets overnight.", 0),
            OnboardingPage("Pick your personality", "Hype Man. Drill Sergeant. Chill Coach. Chaos Goblin. Your app talks how you want it to.", 0),
            OnboardingPage("Rest days aren't failures", "Sick? Exams? Travel? Log it. We protect your streak. We respect your life.", 0)
        )

        val adapter = OnboardingAdapter(pages)
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == pages.size - 1) {
                    binding.btnNext.text = "Get Started"
                } else {
                    binding.btnNext.text = "Next"
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < pages.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        binding.tvSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        prefsManager.onboardingDone = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
