package com.example.pra_project

import com.example.dojomovie.Film
import retrofit2.http.GET

interface ApiService {
    @GET("66cce8acb8f366d2a508")
    suspend fun getFilms(): List<Film>
}