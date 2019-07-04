package com.modori.colorpicker.model

import com.google.gson.annotations.SerializedName

class RandomImageModel {
    @SerializedName("id")
    val id:String = "#3"

    @SerializedName("color")
    val color:String = "#6E633A"

    @SerializedName("urls")
    val urls:Urls? = null



}

class Urls{
    @SerializedName("raw")
    val raw:String = "rawImage"

    @SerializedName("full")
    val full:String = "fullImage"

    @SerializedName("regular")
    val regular:String = "regularImage"

    @SerializedName("small")
    val small:String = "smallImage"

    @SerializedName("thumb")
    val thumb:String = "thumbImage"


}