package com.app.hydraware

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment

class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = view.findViewById<TextView>(R.id.splashTitle)
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.splash_title_anim)
        title.startAnimation(anim)
        Handler(Looper.getMainLooper()).postDelayed({
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragment_container, HomeFragment())
            transaction.commit()
            requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        }, 2000)
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
} 