package com.modori.colorpicker.RA

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.modori.colorpicker.R
import kotlinx.android.synthetic.main.color_items_vertical.view.*

class ColorAdapter(val items: ArrayList<Int>, val context: Context) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    private var colorList:ArrayList<Int> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.color_items_vertical, parent, false))
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("받아온 색", colorList.get(position).toString())
        holder.colorPanel.setBackgroundColor(colorList.get(position))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorPanel: View = view.findViewById(R.id.colorPanel) as View
    }
}