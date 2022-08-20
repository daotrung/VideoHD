package com.daotrung.myapplication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daotrung.myapplication.R
import com.daotrung.myapplication.base.BaseFragment

class FavoriteFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }
    companion object {
        fun newInstance(): FavoriteFragment {
            return FavoriteFragment()
        }
    }
}