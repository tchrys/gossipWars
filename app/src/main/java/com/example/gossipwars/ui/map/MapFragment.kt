package com.example.gossipwars.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.Notifications
import com.example.gossipwars.logic.entities.Region
import com.example.gossipwars.map.src.MapView
import com.example.gossipwars.map.src.Province
import com.example.gossipwars.ui.dialogs.region.RegionDialogFragment

class MapFragment : Fragment() {

    private lateinit var mapViewModel: MapViewModel
    private lateinit var fragmentBarTitle: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
            ViewModelProviders.of(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        fragmentBarTitle = (context as InGameActivity).supportActionBar?.title.toString()
        subscribeToTimer()

        val gameMap = root.findViewById<MapView>(R.id.gameMap)

        gameMap.provinceClicked.observe(viewLifecycleOwner, Observer {
            val region: Region? = GameHelper.findRegionByName(it)
            fragmentManager?.let {
                region?.name?.let { it1 ->
                    RegionDialogFragment(it1).show(
                        it,
                        "regionDialogTag"
                    )
                }
            }
        })

        Notifications.roundOngoing.observe(viewLifecycleOwner, Observer {
            if (it) {
                Province.values().forEach { province ->
                    val region: Region? = GameHelper.findRegionByName(province.name)
                    val idx: Int? = Game.players.value?.indexOfFirst { player -> player.id == region?.occupiedBy?.id }
                    if (idx != null) {
                        gameMap.colorProvince(province, GameHelper.getColorByPlayerIdx(idx))
//                        gameMap.addTitle(province, province.name, Typeface.SANS_SERIF, Color.BLACK)
                    }
                }
            }
        })
        return root
    }

    private fun subscribeToTimer() {
        Notifications.roundTimer.observe(viewLifecycleOwner, Observer {
            if (it > 5) {
                (context as InGameActivity).supportActionBar?.title =
                    getString(R.string.bar_title, fragmentBarTitle, GameHelper.roundTimeToString(it))
            } else {
                (context as InGameActivity).supportActionBar?.title =
                    HtmlCompat.fromHtml(getString(R.string.bar_title_alert, fragmentBarTitle,
                        GameHelper.roundTimeToString(it)), HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        })
    }

}