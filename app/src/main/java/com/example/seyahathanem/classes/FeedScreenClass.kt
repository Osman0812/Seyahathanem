package com.example.seyahathanem.classes

import android.net.Uri


data class FeedScreenClass (val id: String,val placeUri: String? = null, val placeName: String, val latitude: Double, val longitude: Double, val comment: String? = null,val category: String? = null)
