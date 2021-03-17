package com.example.gossipwars.ui.map

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.gossipwars.R
import com.example.gossipwars.map.src.MapView
import com.example.gossipwars.map.src.Province

class MapFragment : Fragment() {

    private lateinit var mapViewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
            ViewModelProviders.of(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        val gameMap = root.findViewById<MapView>(R.id.gameMap)

        Province.values().forEach {
            gameMap.addTitle(it, it.name, Typeface.SANS_SERIF, Color.WHITE)
        }

        context?.let { MapView(it) }

        return root
    }
}