package com.example.seyahathanem.viewModel


class DataModal {
    // variables for storing our image and name.
    var name: String? = null
    var imgUrl: String? = null

    // empty constructor required for Firebase.
    constructor() {}

    // constructor for our object class.
    constructor(name: String?, imgUrl: String?) {
        this.name = name
        this.imgUrl = imgUrl
    }

    // getter and setter methods
    fun getCategoryName(): String? {
        return name
    }

    fun setCategoryName(name: String?) {
        this.name = name
    }

    fun getCategoryImgUrl(): String? {
        return imgUrl
    }

    fun setCategoryImgUrl(imgUrl: String?) {
        this.imgUrl = imgUrl
    }
}