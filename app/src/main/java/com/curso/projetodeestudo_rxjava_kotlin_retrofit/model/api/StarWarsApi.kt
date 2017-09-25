package com.curso.projetodeestudo_rxjava_kotlin_retrofit.model.api

import android.net.Uri
import android.util.Log
import com.curso.projetodeestudo_rxjava_kotlin_retrofit.model.Characters
import com.curso.projetodeestudo_rxjava_kotlin_retrofit.model.MovieClass
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable

class StarWarsApi{

    val service : StarWarsApiDef
    val peopleCache = mutableMapOf<String, Person>()

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
                .baseUrl("http://swapi.co/api/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()
        service = retrofit.create<StarWarsApiDef>(StarWarsApiDef::class.java)
    }
    fun loadMovies() : Observable<MovieClass>{
        return service.listMovies()
                .flatMap { filmResult -> Observable.from(filmResult.results)}
                .flatMap { film -> Observable.just (MovieClass(film.title, film.episodeId, ArrayList<Characters>())) }
    }
    fun loadMoviesfull() : Observable<MovieClass>{
        return service.listMovies()
                .flatMap { filmResult -> Observable.from(filmResult.results) }
                .flatMap { film ->
                    Observable.zip(
                            Observable.just(MovieClass(film.title, film.episodeId, ArrayList<Characters>())),
                            Observable.from(film.personsUrl)
                                    .flatMap { personUrl ->
                                        Observable.concat(
                                                getCache(personUrl),
                                                service.loadPerson(Uri.parse(personUrl).lastPathSegment).doOnNext{
                                                    person -> Log.d("NGVL", "DOWNLOAD----> ${personUrl}")
                                                    peopleCache.put(personUrl, person)
                                                }
                                        ).first()

                                    }
                                    .flatMap { person ->
                                        Observable.just(Characters(person.name, person.gender))
                                    }
                                    .toList(),
                            {movie, characters ->
                                movie.characters.addAll(characters)
                                movie
                            })
                }
    }

    private fun getCache(personUrl : String) : Observable<Person>{
        return Observable.from(peopleCache.keys)
                .filter { key -> key == personUrl }
                .flatMap { key ->
                    Log.d("NGVL", "CACHE---->${key}")
                    Observable.just(peopleCache[personUrl])}
    }
}