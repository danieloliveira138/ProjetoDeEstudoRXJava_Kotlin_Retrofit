package com.curso.projetodeestudo_rxjava_kotlin_retrofit.model.api

import com.google.gson.annotations.SerializedName

data class FilmResult(val results : List<Film>)

data class Film(val title : String,
                @SerializedName("episode_id") val episodeId : Int,
                @SerializedName("characters") val personsUrl : List<String>)

data class Person(val name : String, val gender : String)

