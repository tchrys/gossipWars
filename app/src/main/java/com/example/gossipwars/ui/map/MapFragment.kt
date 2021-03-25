package com.example.gossipwars.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.Region
import com.example.gossipwars.map.src.MapView
import com.example.gossipwars.ui.dialogs.region.RegionDialogFragment

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
        gameMap.provinceClicked.observe(viewLifecycleOwner, Observer {
            val region: Region? = GameHelper.findRegionByName(it)
//            var info: String? = region?.name
//            info += ": " + region?.getNeighborsList()?.map { region -> region.name }.joinToString(",")
//            Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
            fragmentManager?.let {
                region?.name?.let { it1 ->
                    RegionDialogFragment(it1).show(
                        it,
                        "regionDialogTag"
                    )
                }
            }
        })

//        Province.values().forEach {
//            gameMap.addTitle(it, it.name, Typeface.SANS_SERIF, Color.BLACK)
//        }

//        context?.let { MapView(it) }

        return root
    }

}