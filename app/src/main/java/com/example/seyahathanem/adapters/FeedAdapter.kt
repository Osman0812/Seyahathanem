package com.example.seyahathanem.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FeedRowBinding
import com.example.seyahathanem.activities.MainActivity
import com.example.seyahathanem.classes.FeedScreenClass
import com.example.seyahathanem.classes.SwipeToDeleteCallback
import com.example.seyahathanem.fragments.MapsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class FeedAdapter (private val recyclerView: RecyclerView,private val context: Context, private val categoryList: ArrayList<FeedScreenClass>,private val cName: String) : RecyclerView.Adapter<FeedAdapter.CategoryHolder>() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    class CategoryHolder(val binding: FeedRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val binding = FeedRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        firestore = Firebase.firestore
        auth = Firebase.auth

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

        holder.binding.commentText.text = categoryList.get(position).comment

        /*
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




         */

        holder.binding.locationIcon.setOnClickListener {
            val latitude = categoryList.get(position).latitude
            val longitude = categoryList.get(position).longitude

            val mapsFragment = MapsFragment.newInstance(latitude, longitude)
            (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.feedLayout,mapsFragment)
                .addToBackStack(null)
                .commit()

        }





    }
    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(
        object : SwipeToDeleteCallback() {
            override fun onSwipeToDelete(position: Int) {
                val category = categoryList[position]
                categoryList.removeAt(position)
                notifyItemRemoved(position)


                val ref = firestore.collection("Users").document(auth.currentUser!!.email.toString())
                ref.collection(cName).document(category.id).delete().addOnSuccessListener {
                    ref.collection(cName).get().addOnSuccessListener {snap->
                        if (snap.isEmpty){
                            ref.get().addOnCompleteListener {task->
                                if (task.isSuccessful){
                                    val document = task.result
                                    if (document.exists()){
                                        val collections = document["collections"] as ArrayList<String>?

                                        collections?.remove(category.category)

                                        ref.update("collections",collections)
                                            .addOnSuccessListener {
                                                Toast.makeText(context,category.placeName + " deleted!", Toast.LENGTH_LONG).show()
                                                val intent = Intent(context,MainActivity::class.java)
                                                context.startActivity(intent)

                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context,it.message, Toast.LENGTH_LONG).show()
                                            }


                                    }
                                }
                            }

                        }
                    }

                }

            }
        }
    )


    fun attachSwipeToDelete() {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


}