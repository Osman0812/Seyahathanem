package com.example.seyahathanem.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.mezunproject.R
import com.example.mezunproject.databinding.SocialRowBinding
import com.example.seyahathanem.activities.ProfileActivity
import com.example.seyahathanem.classes.ShareClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SocialAdapter(val context: Context, private val usersList: ArrayList<ShareClass>) : RecyclerView.Adapter<SocialAdapter.ShareHolder>() {

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ShareHolder, position: Int) {
        if (usersList.get(position).email == auth.currentUser!!.email.toString()){
            holder.binding.addFriend.visibility = View.INVISIBLE
        }
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

        holder.binding.addFriend.setOnClickListener {
            val alert = AlertDialog.Builder(holder.itemView.context)
            alert.setMessage("Are you sure you want to send friend request ${usersList.get(position).name}?")
            alert.setPositiveButton("Yes") {_,_ ->
                friendRequest(usersList.get(position).email)
                holder.binding.addFriend.isClickable = false
            }
            alert.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()

            }
            alert.show()
            
        }


    }
    private fun friendRequest(email: String){

        firestore.collection("Users").document(email).collection("Friends").get().addOnCompleteListener {
            if (it.isSuccessful){
                var flag = 0
                val documents = it.result

                if (documents != null){
                    for (document in documents) {
                        println(email)
                        val friend = document.get("email") as String
                        if (friend == auth.currentUser!!.email.toString()){
                            flag = 1
                            Toast.makeText(context,"Already friends!",Toast.LENGTH_LONG).show()
                        }
                    }
                }

                if (flag == 0){
                    //Send request
                    val hashMap = hashMapOf<String,Any>()
                    hashMap.put("request",auth.currentUser!!.email.toString())
                    firestore.collection("Users").document(email).update(hashMap).addOnSuccessListener {

                        Toast.makeText(context,"Request sent",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }




    }
}