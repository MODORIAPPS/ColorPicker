package com.modori.colorpicker.RA

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.recyclerview.widget.RecyclerView
import com.modori.colorpicker.R
import kotlinx.android.synthetic.main.color_items_vertical.view.*

class ColorAdapter(private val items: List<Int>, private val context: Context) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    private var colorList:List<Int> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.color_items_vertical, parent, false))
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val hexColor:String = String.format("#%06X", (0xFFFFFF and colorList[position]))
        val color:Int = Color.parseColor(hexColor)
        val strRGB:String = "RGB( ${color.red} , ${color.green} , ${color.blue} )"
        //val strHsl:Stirng =

        //Log.d("RED", color.red.toString())
        //Log.d("받아온 색", colorList[position].toString())

        holder.colorHex.text = hexColor
        holder.colorPanel.setBackgroundColor(colorList[position])
        holder.colorPanel.setOnClickListener {
            Toast.makeText(context,"길게 눌러 복사합니다.", Toast.LENGTH_SHORT).show()
        }
        holder.colorPanel.setOnLongClickListener {
            return@setOnLongClickListener setClipBoardLink(context, "HEX : $hexColor, $strRGB")

        }
        //Color.colorTohs

        holder.colorRGB.text = strRGB



    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorPanel: View = view.findViewById(R.id.colorPanel) as View
        val colorHex: TextView = view.findViewById(R.id.colorHex) as TextView
        val colorRGB:TextView = view.findViewById(R.id.colorRGB) as TextView
        val colorHsl:TextView = view.findViewById(R.id.colorHsl) as TextView
    }

    private fun setClipBoardLink(context: Context, link:String):Boolean{
        val clipboardManager:ClipboardManager = context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData:ClipData = ClipData.newPlainText("label", link)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context,link + "를 클립보드에 복사하였습니다.", Toast.LENGTH_LONG).show()

        return true
    }



}

