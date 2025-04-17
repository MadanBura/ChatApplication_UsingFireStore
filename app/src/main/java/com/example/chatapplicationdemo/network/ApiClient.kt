import com.example.chatapplicationdemo.network.GoogleApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

object RetrofitInstance {

    private const val BASE_URL = "https://fcm.googleapis.com/"

    val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val googleApiService: GoogleApiService by lazy {
        retrofit.create(GoogleApiService::class.java)
    }
}
