package com.arophix.mvvm.example.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter


@BindingAdapter("imageFromBase64")
fun bindImageFromBase64(imageView: ImageView, imageBase64: String?) {
    if (!imageBase64.isNullOrEmpty()) {
        val decodedString: ByteArray = Base64.decode(imageBase64, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        imageView.setImageBitmap(decodedByte)
    }
}

