package com.curso.projetodeestudo_rxjava_kotlin_retrofit.model

    data class MovieClass (val title : String, val episodeId : Int , val characters : MutableList<Characters>)

data class Characters(val name : String, val gender : String){
    override fun toString(): String{
        return "${name} - ${gender}"
    }
}
