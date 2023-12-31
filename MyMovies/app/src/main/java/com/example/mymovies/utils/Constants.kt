package com.example.mymovies.utils

import java.util.Locale

class Constants {
    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_URL = "http://image.tmdb.org/t/p/w500"
        const val AUTHENTICATION_KEY = "Your Key Here"
        const val POP_ANIMATION_DURATION = 500L
        lateinit var ID_TO_STRING_GENRES : Map<Int, String>

        lateinit var STRING_TO_ID_GENRES : Map<String, Int>

        fun getLocale() : String{
            if(Locale.getDefault().language == "iw") {
                return "he-IL"
            }
            return "en-US"
        }

        fun setGenresDict(genresDict : Map<Int, String>){
            ID_TO_STRING_GENRES = genresDict
            STRING_TO_ID_GENRES = ID_TO_STRING_GENRES.entries.associate { (key, value) -> value to key }
        }
    }
}