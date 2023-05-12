package com.example.seyahathanem.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.example.mezunproject.R
import com.example.seyahathanem.viewModel.DataModal

internal class GridRVAdapter(
    // on below line we are creating two
    // variables for course list and context
    private val courseList: ArrayList<DataModal>,
    private val context: Context
) :
    BaseAdapter() {
    // in base adapter class we are creating variables
    // for layout inflater, course image view and course text view.
    private var layoutInflater: LayoutInflater? = null
    private lateinit var categoryTV: TextView
    private lateinit var categoryIV: ImageView

    // below method is use to return the count of course list
    override fun getCount(): Int {
        return courseList.size
    }

    // below function is use to return the item of grid view.
    override fun getItem(position: Int): Any? {
        return null
    }

    // below function is use to return item id of grid view.
    override fun getItemId(position: Int): Long {
        return 0
    }

    // in below function we are getting individual item of grid view.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View{
        var convertView = convertView

        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        // on the below line we are checking if convert view is null.
        // If it is null we are initializing it.
        if (convertView == null) {
            // on below line we are passing the layout file
            // which we have to inflate for each item of grid view.
            convertView = layoutInflater!!.inflate(R.layout.gridview_item, null)
        }
        // on below line we are initializing our course image view
        // and course text view with their ids.
        categoryIV = convertView!!.findViewById(R.id.categoryImage)
        categoryTV = convertView.findViewById(R.id.categoryName)
        // on below line we are setting image for our course image view.
        //courseIV.setImageResource(courseList.get(position).courseImg)
        // on below line we are setting text in our course text view.
        categoryTV.text = courseList.get(position).name
        categoryIV.setImageResource(courseList.get(position).img)

        // at last we are returning our convert view.
        return convertView
    }
}