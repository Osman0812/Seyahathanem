package com.example.seyahathanem.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mezunproject.R
import com.example.mezunproject.databinding.SocialRowBinding
import com.example.seyahathanem.activities.ProfileActivity
import com.example.seyahathanem.classes.ShareClass
import com.example.seyahathanem.classes.SwipeToDeleteCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class FriendsAdapter(val recyclerView: RecyclerView, val context: Context, private val usersList: ArrayList<ShareClass>) : RecyclerView.Adapter<FriendsAdapter.ShareHolder>() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    class ShareHolder(val binding : SocialRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareHolder {
        val binding = SocialRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        auth = Firebase.auth
        firestore = Firebase.firestore
        return ShareHolder(binding)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    override fun onBindViewHolder(holder: ShareHolder, position: Int) {
        holder.binding.addFriend.visibility = View.INVISIBLE
        holder.binding.emailText.text = usersList.get(position).email
        //holder.binding.surnameText.text = usersList.get(position).surname
        holder.binding.nameText.text = usersList.get(position).name + " " + usersList.get(position).surname
        if (usersList.get(position).pictureUrl != null){
            Picasso.get().load(usersList.get(position).pictureUrl).into(holder.binding.profileView)
        }else {
            Picasso.get().load(R.drawable.noimg).into(holder.binding.profileView)
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("email",usersList.get(position).email)
            holder.itemView.context.startActivity(intent)
        }
        holder.binding.emailText.setOnLongClickListener{


            val alert = AlertDialog.Builder(holder.itemView.context)
            alert.setMessage("Are you sure you want to contact with ${usersList.get(position).name}?")
            alert.setPositiveButton("Yes") {_,_ ->
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${usersList.get(position).email}")
                }
                holder.itemView.context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
            }
            alert.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()

            }
            alert.show()
            true

        }


    }
    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(
        object : SwipeToDeleteCallback() {
            override fun onSwipeToDelete(position: Int) {
                if (usersList.isNotEmpty() && position >= 0 && position < usersList.size) {
                    // Bu position'daki arkadaşı çekiyoruz.
                    val friendToRemove = usersList[position]

                    // Firestore'dan arkadaşlığı kaldırıyoruz.
                    val currentUserEmail = auth.currentUser!!.email.toString()

                    val currentUserFriendDoc = firestore.collection("Users").document(currentUserEmail).collection("Friends").document(friendToRemove.email)
                    val friendToRemoveFriendDoc = firestore.collection("Users").document(friendToRemove.email).collection("Friends").document(currentUserEmail)

                    firestore.runTransaction { transaction ->
                        transaction.delete(currentUserFriendDoc)
                        transaction.delete(friendToRemoveFriendDoc)
                    }.addOnSuccessListener {
                        Toast.makeText(context,"Friendship removed successfully",Toast.LENGTH_LONG).show()
                        Log.d(ContentValues.TAG, "Friendship removed successfully")
                        // Silme işlemi tamamlandıktan sonra arkadaş listesini güncelle

                    }.addOnFailureListener { e ->
                        Log.w(ContentValues.TAG, "Error removing friendship", e)
                    }
                    // Lokal listeden de kaldırıyoruz.
                    usersList.removeAt(position)
                    notifyItemRemoved(position)
                } else {
                    Log.e(ContentValues.TAG, "Invalid position. Cannot remove item.")
                }
            }
        }
    )


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<ShareClass>) {
        usersList.clear()
        usersList.addAll(newList)
        notifyDataSetChanged()
    }
    fun attachSwipeToDelete() {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}