package com.highcom.comicmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.highcom.comicmemo.datamodel.RakutenBookData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class RakutenBookActivity : AppCompatActivity() {

    private val itemInterface by lazy { createService() }

    interface ItemInterface {
        @GET("BooksBook/Search/20170404?format=json&booksGenreId=001001&sort=-releaseDate&hits=5")
        fun items(@Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>
    }

    fun createService(): ItemInterface {
        val baseApiUrl = "https://app.rakuten.co.jp/services/api/"

        val httpLogging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClientBuilder = OkHttpClient.Builder().addInterceptor(httpLogging)

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseApiUrl)
            .client(httpClientBuilder.build())
            .build()

        return retrofit.create(ItemInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rakuten_book)

        val appId = getString(R.string.rakuten_app_id)
        getBookTrend(appId)
    }

    private fun getBookTrend(appId: String){
        itemInterface.items(appId).enqueue(object : retrofit2.Callback<RakutenBookData> {
            override fun onFailure(call: retrofit2.Call<RakutenBookData>?, t: Throwable?) {
                Log.d("TAG_DEBUG", t.toString())
            }

            override fun onResponse(call: retrofit2.Call<RakutenBookData>?, response: retrofit2.Response<RakutenBookData>) {
                if (response.isSuccessful) {
                    response.body()?.let {

                        var items = mutableListOf<String>()
                        var res = response.body()?.Items?.iterator()

//                        var title = response.body()!!.title

                        if (res != null) {
                            for (item in res) {
                                items.add(item.Item.title)
                            }
                        }

                        val adapter = ArrayAdapter(this@RakutenBookActivity, android.R.layout.simple_list_item_1, items)
                        val list: ListView = findViewById(R.id.trend_list_view)
                        list.adapter = adapter
                    }
                }
            }
        })
    }
}