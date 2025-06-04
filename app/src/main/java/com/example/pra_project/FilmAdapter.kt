package com.example.pra_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dojomovie.Film

class FilmAdapter(private val filmList: List<Film>) :
    RecyclerView.Adapter<FilmAdapter.FilmViewHolder>() {

    inner class FilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_item_name)
        val price: TextView = itemView.findViewById(R.id.tv_item_price)
        val image: ImageView = itemView.findViewById(R.id.tv_item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return FilmViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val film = filmList[position]
        holder.title.text = film.title
        holder.price.text = "Rp ${film.price}"

        // Jika kamu pakai Glide untuk gambar dari URL
        Glide.with(holder.itemView.context)
            .load(film.image)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            val fragment = MovieDetailsFragment.newInstance(film.id)

            // Get the activity and replace the fragment
            val activity = holder.itemView.context as? MainActivity
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    override fun getItemCount(): Int = filmList.size
}