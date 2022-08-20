package com.daotrung.myapplication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daotrung.myapplication.R


class SelectedMusicListFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(): SelectedMusicListFragment {
            val args = Bundle()
            val fragment = SelectedMusicListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private fun replaceFragment(fragment: Fragment) {

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selected_music_list, container, false)
    }

}