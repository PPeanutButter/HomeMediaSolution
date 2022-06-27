package com.peanut.ted.ed

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv: TextView = itemView.findViewById(R.id.tv_name)
    val date: TextView = itemView.findViewById(R.id.date)
    val iv: ImageView = itemView.findViewById(R.id.card_img)
    val card: MaterialCardView = itemView.findViewById(R.id.card)
    val actionPlay: ImageView = itemView.findViewById(R.id.action_play)
    val actionLink: ImageView = itemView.findViewById(R.id.action_link)
    val actionBook: ImageView = itemView.findViewById(R.id.action_book)
}