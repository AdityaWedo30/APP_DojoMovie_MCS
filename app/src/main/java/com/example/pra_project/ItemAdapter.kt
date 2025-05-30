package com.example.pra_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(private val itemList: ArrayList<Item>) : RecyclerView.Adapter<ItemAdapter.myViewHolder>() {


    class myViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val itemImageView: ImageView = itemView.findViewById(R.id.tv_item_image)
        val itemNameView: TextView = itemView.findViewById(R.id.tv_item_name)
        val itemPriceView: TextView = itemView.findViewById(R.id.tv_item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return myViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentItem = itemList[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.itemImage)  // assuming itemImage is a URL string
            .into(holder.itemImageView)

        holder.itemNameView.text = currentItem.itemName
        holder.itemPriceView.text = formatToRupiahNoDecimal(currentItem.itemPrice.toInt())
    }
    fun formatToRupiahNoDecimal(number: Int): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(number)
    }
}