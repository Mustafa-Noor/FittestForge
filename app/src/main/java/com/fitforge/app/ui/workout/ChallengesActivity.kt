package com.fitforge.app.ui.workout

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.data.ChallengeData
import com.fitforge.app.data.models.Challenge
import com.fitforge.app.databinding.ActivityChallengesBinding

class ChallengesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChallengesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupChallengeList(binding.rvWeeklyChallenges, ChallengeData.getWeekly())
        setupChallengeList(binding.rvMonthlyChallenges, ChallengeData.getMonthly())
    }

    private fun setupChallengeList(rv: androidx.recyclerview.widget.RecyclerView, challenges: List<Challenge>) {
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ChallengeAdapter(challenges) { challenge ->
            val intent = Intent(this, ChallengeDetailActivity::class.java)
            intent.putExtra("challenge_id", challenge.id)
            startActivity(intent)
        }
    }
}
