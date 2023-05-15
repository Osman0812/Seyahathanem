package com.example.seyahathanem.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FeedRowBinding
import com.example.seyahathanem.classes.FeedScreenClass
import com.example.seyahathanem.fragments.MapsFragment
import com.squareup.picasso.Picasso


class FeedAdapter (private val context: Context, private val categoryList: ArrayList<FeedScreenClass>) : RecyclerView.Adapter<FeedAdapter.CategoryHolder>(){

    class CategoryHolder(val binding: FeedRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val binding = FeedRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CategoryHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.nameText.text = categoryList.get(position).placeName
        if (categoryList.get(position).placeUri != null){
            Picasso.get().load(categoryList.get(position).placeUri).into(holder.binding.categoryImage)
        }else{
            Picasso.get().load(R.drawable.noimg).into(holder.binding.categoryImage)
        }
        val spannableString = SpannableString(holder.binding.location.text.toString())
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val latitude = categoryList.get(position).latitude
                val longitude = categoryList.get(position).longitude

                val mapsFragment = MapsFragment.newInstance(latitude, longitude)
                (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.feedLayout,mapsFragment)
                    .addToBackStack(null)
                    .commit()

            }
        }
        spannableString.setSpan(clickableSpan,0,holder.binding.location.text.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.binding.location.text = spannableString
        holder.binding.location.movementMethod = LinkMovementMethod.getInstance()

    }

}