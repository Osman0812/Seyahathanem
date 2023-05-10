package com.example.seyahathanem.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.mezunproject.R
import com.example.mezunproject.databinding.GridviewItemBinding
import com.example.seyahathanem.viewModel.DataModal
import com.squareup.picasso.Picasso

class CoursesGVAdapter(context: Context, private val dataModalArrayList: ArrayList<DataModal>) :
    ArrayAdapter<DataModal>(context, 0, dataModalArrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflate the layout for our item of list view.
        var listItemView = convertView
        if (listItemView == null) {
            listItemView =
                LayoutInflater.from(context).inflate(R.layout.gridview_item, parent, false)
        }

        // Get data from the ArrayList inside our modal class.
        val dataModal = getItem(position)

        // Initialize our UI components of list view item.
        val cName = listItemView?.findViewById<TextView>(R.id.categoryName)
        val cImage = listItemView?.findViewById<ImageView>(R.id.categoryImage)

        // Set data to our views.
        // Set data to our TextView.
        if (cName != null) {
            cName.text = dataModal?.name
        }

        // Load image from URL in our ImageView using Picasso.
        Picasso.get().load(dataModal?.imgUrl).into(cImage)

        // Add item click listener for our item of list view.
        listItemView?.setOnClickListener {
            // On the item click on our list view,
            // we are displaying a toast message.
            Toast.makeText(
                context,
                "Item clicked is : " + dataModal?.name,
                Toast.LENGTH_SHORT
            ).show()
        }

        return listItemView!!
    }
}