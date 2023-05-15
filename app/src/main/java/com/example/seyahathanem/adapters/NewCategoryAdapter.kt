package com.example.seyahathanem.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mezunproject.R
import com.example.mezunproject.databinding.CategoryRowBinding
import com.example.seyahathanem.classes.NewCategory
import com.squareup.picasso.Picasso

class NewCategoryAdapter(private val categoryList: ArrayList<NewCategory>): RecyclerView.Adapter<NewCategoryAdapter.ViewHolder>(){

    class ViewHolder(val binding: CategoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.newCategoryNameTextView.text = categoryList.get(position).categoryName
        Picasso.get().load(categoryList.get(position).downloadUrl).into(holder.binding.createdCategoryPictureView)

    }
}