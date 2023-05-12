package com.example.seyahathanem.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mezunproject.databinding.FeedRowBinding
import com.example.seyahathanem.classes.FeedScreenClass
import com.squareup.picasso.Picasso

class FeedAdapter (private val categoryList: ArrayList<FeedScreenClass>) : RecyclerView.Adapter<FeedAdapter.CategoryHolder>(){

    class CategoryHolder(val binding: FeedRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val binding = FeedRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CategoryHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.binding.nameText.text = categoryList.get(position).placeName
        Picasso.get().load(categoryList.get(position).placeUri).into(holder.binding.categoryImage)
    }

}