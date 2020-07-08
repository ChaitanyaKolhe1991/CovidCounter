import com.example.covidcounter.api.ApiService

class ApiHelper(private val apiService: ApiService) {
    suspend fun getSummary() = apiService.getSummary()
}