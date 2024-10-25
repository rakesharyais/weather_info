package com.example.weatherinfo

import com.example.weatherdata.model.GeoLocationResponse
import com.example.weatherdata.model.Main
import com.example.weatherdata.model.Weather
import com.example.weatherdata.model.WeatherResponse
import com.example.weatherinfo.api.WeatherApi
import com.example.weatherinfo.repository.WeatherRepository
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import retrofit2.Response

class WeatherRepositoryTest {

    // Mocked Weather API
    @Mock
    private lateinit var api: WeatherApi

    // Repository under test
    private lateinit var repository: WeatherRepository

    @Before
    fun setUp() {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this)

        // Initialize the repository with the mocked API
        repository = WeatherRepository(api)
    }

    @Test
    fun `getCoordinates returns first result when API call is successful`() = runBlockingTest {
        // Given: A mocked successful API response for coordinates
        val mockGeoLocationResponse = listOf(
            GeoLocationResponse(
                name = "San Francisco",
                lat = 37.7749,
                lon = -122.4194,
                country = "US",
                state = "California"
            )
        )

        // Simulate a successful API response using Response.success()
        val response = Response.success(mockGeoLocationResponse)

        // When: The API's getCoordinates method is called
        `when`(api.getCoordinates(anyString(), anyInt(), anyString())).thenReturn(response)

        // Act: Call the repository method
        val result = repository.getCoordinates("San Francisco", "api-key")

        // Then: The result should not be null and should match the mock data
        assertNotNull(result)
        assertEquals("San Francisco", result?.name)
        assertEquals(37.7749, result?.lat!!, 0.0)
        assertEquals(-122.4194, result.lon, 0.0)
        assertEquals("US", result.country)
        assertEquals("California", result.state)
    }


    @Test
    fun `getCoordinates returns null when API call fails`() = runBlockingTest {
        // Given: A mocked failed API response
        val response = Response.error<List<GeoLocationResponse>>(404, mock())

        // When: The API's getCoordinates is called
        `when`(api.getCoordinates(anyString(), anyInt(), anyString())).thenReturn(response)

        // Act: Call the repository method
        val result = repository.getCoordinates("InvalidCity", "api-key")

        // Then: The result should be null
        assertNull(result)
    }

    @Test
    fun `getWeatherDetails returns weather data when API call is successful`() = runBlockingTest {
        // Given: A mocked successful API response for weather details
        val mockWeatherResponse = WeatherResponse(
            weather = listOf(
                Weather(description = "broken clouds", icon = "04d")
            ),
            base = "stations",
            main = Main(
                temp = 293.4,
                feels_like = 293.3,
                temp_min = 290.0,
                temp_max = 295.0,
                pressure = 1010,
                humidity = 70,
                sea_level = 1010,  // Optional field
                grnd_level = 981   // Optional field
            ),
            visibility = 10000,
            dt = 1626711315,
            timezone = -25200,
            id = 5392171,
            name = "San Francisco",
            cod = 200
        )

        // Create a successful mock response
        val response = Response.success(mockWeatherResponse)

        // Mock the API's getWeatherDetails method
        `when`(api.getWeatherDetails(anyDouble(), anyDouble(), anyString())).thenReturn(response)

        // Act: Call the repository method
        val result = repository.getWeatherDetails(37.7749, -122.4194, "api-key")

        // Then: The result should not be null and should match the mock data
        assertNotNull(result)
        assertEquals(mockWeatherResponse, result)
        assertEquals(293.4, result?.main?.temp!!, 0.0)
        assertEquals("broken clouds", result?.weather?.first()?.description)
    }



    @Test
    fun `getWeatherDetails returns null when API call fails`() = runBlockingTest {
        // Given: A mocked failed API response
        val response = Response.error<WeatherResponse>(500, mock())

        // When: The API's getWeatherDetails is called
        `when`(api.getWeatherDetails(anyDouble(), anyDouble(), anyString())).thenReturn(response)

        // Act: Call the repository method
        val result = repository.getWeatherDetails(37.7749, -122.4194, "api-key")

        // Then: The result should be null
        assertNull(result)
    }
}