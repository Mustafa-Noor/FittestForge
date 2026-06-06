package com.fitforge.app.ui.workout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitforge.app.data.ChallengeData
import com.fitforge.app.data.models.Challenge
import com.fitforge.app.databinding.FragmentChallengesBinding

class ChallengesFragment : Fragment() {
    private var _binding: FragmentChallengesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupChallengeList(binding.rvWeeklyChallenges, ChallengeData.getWeekly())
        setupChallengeList(binding.rvMonthlyChallenges, ChallengeData.getMonthly())
    }

    private fun setupChallengeList(rv: androidx.recyclerview.widget.RecyclerView, challenges: List<Challenge>) {
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = ChallengeAdapter(challenges) { challenge ->
            val intent = Intent(requireContext(), ChallengeDetailActivity::class.java)
            intent.putExtra("challenge_id", challenge.id)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
