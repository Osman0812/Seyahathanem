package com.example.seyahathanem.viewModel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel

class ImageKeeper() : ViewModel() {

    var selectedImageBitmap : Bitmap? = null
    var placeName : String? = null
    var selectedCategoryBitmap : Bitmap? = null
    var imageUri : Uri? = null



}