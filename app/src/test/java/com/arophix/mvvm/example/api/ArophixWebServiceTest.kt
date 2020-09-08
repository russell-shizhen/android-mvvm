package com.arophix.mvvm.example.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

@RunWith(JUnit4::class)
class ArophixWebServiceTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: ArophixWebService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
                .baseUrl(mockWebServer.url(""))
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
                .create(ArophixWebService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun requestDnsServerLocations() {
        runBlocking {
            enqueueResponse("arophix_response_success.xml", 200)
            val resultResponse = service.getDnsServerLocations().body()

            val request = mockWebServer.takeRequest()
            Assert.assertNotNull(resultResponse)
            Assert.assertThat(request.path, CoreMatchers.`is`("/AdhocCPS/dns/express_dns/list"))
        }
    }

    @Test
    fun getArophixDnsApiResponse_success() {
        runBlocking {

            enqueueResponse("arophix_response_success.xml", 200)

            val resultResponse = service.getDnsServerLocations().body()

            val xmlButtonText = resultResponse!!.buttonText
            val xmlIcons = resultResponse.xmlIcons
            val xmlLocations = resultResponse.xmlLocations

            Assert.assertThat(xmlButtonText, CoreMatchers.`is`("Another text to be handled on client side."))
            Assert.assertThat(xmlIcons!!.xmlIconList!!.size, CoreMatchers.`is`(6))
            Assert.assertThat(xmlLocations!!.xmlLocationList!!.size, CoreMatchers.`is`(6))
        }
    }

    @Test
    fun getArophixDnsApiResponse_error() {
        runBlocking {

            enqueueResponse("arophix_response_error.xml", 403)
            
            val resultResponse = service.getDnsServerLocations()
            val message = resultResponse.errorBody()!!.string()

            Assert.assertThat(resultResponse.isSuccessful, CoreMatchers.`is`(false))
            Assert.assertThat(resultResponse.code(), CoreMatchers.`is`(403))

            val inputStream = javaClass.classLoader.getResourceAsStream("api-response/arophix_response_error.xml")
            val source = inputStream.source().buffer()

            println("errorBodyString: \n${message}")
            Assert.assertThat(message, CoreMatchers.`is`(source.readString(Charsets.UTF_8)))
        }
    }

    private fun enqueueResponse(fileName: String, responseCode: Int, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader.getResourceAsStream("api-response/$fileName")
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        println("Error response: $source.readString(Charsets.UTF_8)")
        mockWebServer.enqueue(mockResponse.setResponseCode(responseCode).setBody(source.readString(Charsets.UTF_8)))
    }
}