package com.example.gossipwars.ui.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.NewsFeedInfo
import com.example.gossipwars.logic.entities.Snapshots
import com.richpath.RichPath
import com.richpath.RichPathView
import com.richpathanimator.RichPathAnimator

class NewsFeedListAdapter(
    context: OverviewFragment,
    private val newsFeedList: ArrayList<NewsFeedInfo>
) : RecyclerView.Adapter<NewsFeedListAdapter.NewsFeedViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context.activity)

    inner class NewsFeedViewHolder(
        itemView: View,
        val mAdapter: NewsFeedListAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val newsFeedTitle: TextView = itemView.findViewById(R.id.newsFeedTitle)
        val newsFeedMap: RichPathView = itemView.findViewById(R.id.newsFeedMap)
        val newsFeedContent: TextView = itemView.findViewById(R.id.newsFeedContent)

        override fun onClick(view: View?) {
            val mPosition = layoutPosition
            newsFeedList[mPosition]
            mAdapter.notifyDataSetChanged()
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsFeedViewHolder {
        val mItemView = mInflater.inflate(R.layout.newsfeed_items, parent, false)
        return NewsFeedViewHolder(mItemView, this)
    }

    override fun onBindViewHolder(holder: NewsFeedViewHolder, position: Int) {
        val mCurrent = newsFeedList[position]
        holder.newsFeedTitle.text = mCurrent.title
        holder.newsFeedContent.text = mCurrent.content

        if (mCurrent.firstRegion == null) {
            holder.newsFeedMap.visibility = View.GONE
        } else {
            holder.newsFeedMap.setVectorDrawable(Snapshots.getDrawableForRegion(mCurrent.firstRegion))
            if (mCurrent.secondRegion != null) {
                val secondRegion: RichPath? =
                    holder.newsFeedMap.findRichPathByName(GameHelper.findRegionById(mCurrent.secondRegion)?.name)
                secondRegion?.let {
                    RichPathAnimator.animate(it)
                        .interpolator(AccelerateDecelerateInterpolator())
                        .fillColor(it.fillColor, R.color.grey)
                        .start()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return newsFeedList.size
    }

}