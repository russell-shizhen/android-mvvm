package com.arophix.mvvm.example.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.arophix.mvvm.example.utilities.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.*

class ArophixDnsResponseDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var arophixDnsResponseDao: ArophixDnsResponseDao

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        arophixDnsResponseDao = database.arophixDnsResponseDao()

        database.arophixDnsResponseDao().insertButtonText(testButtonText);
        database.arophixDnsResponseDao().insertDnsLocation(testDnsLocation);
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndGetOperations() = runBlocking {
        val response2 = ButtonText("101", "Place another text to refresh button.")

        arophixDnsResponseDao.insertButtonText(response2)
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getButtonTexts()).size, CoreMatchers.equalTo(2))

        val location2 = DnsLocation("Singapore", "20", "6", "dummy code", "192.168.2.1", "101")
        arophixDnsResponseDao.insertDnsLocation(location2)
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getDnsLocations()).size, CoreMatchers.equalTo(2))
    }

    @Test
    fun testInsertAndDeleteOperations() = runBlocking {
        val response2 = ButtonText("101", "Place another text to refresh button.")
        arophixDnsResponseDao.insertButtonText(response2)
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getButtonTexts()).size, CoreMatchers.equalTo(2))
        arophixDnsResponseDao.deleteButtonText(response2)
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getButtonTexts()).size, CoreMatchers.equalTo(1))

        val location2 = DnsLocation("Singapore", "20", "6", "dummy code", "192.168.2.1", "101")
        arophixDnsResponseDao.insertDnsLocation(location2)
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getDnsLocations()).size, CoreMatchers.equalTo(2))
        arophixDnsResponseDao.deleteDnsLocation(location2)
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getDnsLocations()).size, CoreMatchers.equalTo(1))
    }

    @Test
    fun testInsertLocationList() = runBlocking {
        arophixDnsResponseDao.deleteButtonText(testButtonText)
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getButtonTexts()).size, CoreMatchers.equalTo(0))

        arophixDnsResponseDao.insertDnsLocations(testDnsLocations)
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getDnsLocations()).size, CoreMatchers.equalTo(6))
    }

    @Test
    fun testLastEntityDetailValue() {
        Assert.assertTrue(getValue(arophixDnsResponseDao.getLastButtonText("100")).text == "Another text to be handled on client side.")
        Assert.assertTrue(getValue(arophixDnsResponseDao.getLastDnsLocations("100")).size == 1)
        Assert.assertTrue(getValue(arophixDnsResponseDao.getLastDnsLocations("100"))[0].name == "Los Angeles")
        Assert.assertTrue(getValue(arophixDnsResponseDao.getLastDnsLocations("100"))[0].sortOrder == "80")
        Assert.assertTrue(getValue(arophixDnsResponseDao.getLastDnsLocations("100"))[0].iconId == "5")
        Assert.assertTrue(getValue(arophixDnsResponseDao.getLastDnsLocations("100"))[0].iconBase64Code == "iVBORw0KGgoAAAANSUhEUgAAABAAAAALCAIAAAD5gJpuAAAABGdBTUEAAK/I NwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAHz SURBVHjaYkxOP8IAB//+Mfz7w8Dwi4HhP5CcJb/n/7evb16/APL/gRFQDiAA w3JuAgAIBEDQ/iswEERjGzBQLEru97ll0g0+3HvqMn1SpqlqGsZMsZsIe0SI CA5gt5a/AGIEarCPtFh+6N/ffwxA9OvP/7//QYwff/6fZahmePeB4dNHhi+f Gb59Y4zyvHHmCEAAAW3YDzQYaJJ93a+vX79aVf58//69fvEPlpIfnz59+vDh w7t37968efP3b/SXL59OnjwIEEAsDP+YgY53b2b89++/awvLn98MDi2cVxl+ /vl6mituCtBghi9f/v/48e/XL86krj9XzwEEEENy8g6gu22rfn78+NGs5Ofr 16+ZC58+fvyYwX8rxOxXr169fPny+fPn1//93bJlBUAAsQADZMEBxj9/GBxb 2P/9+S/R8u3vzxuyaX8ZHv3j8/YGms3w8ycQARmi2eE37t4ACCDGR4/uSkrK AS35B3TT////wADOgLOBIaXIyjBlwxKAAGKRXjCB0SOEaeu+/y9fMnz4AHQx CP348R/o+l+//sMZQBNLEvif3AcIIMZbty7Ly6t9ZmXl+fXj/38GoHH/UcGf P79//BBiYHjy9+8/oUkNAAHEwt1V/vI/KBY/QSISFqM/GBg+MzB8A6PfYC5E FiDAABqgW776MP0rAAAAAElFTkSuQmCC")
        Assert.assertTrue(getValue(arophixDnsResponseDao.getLastDnsLocations("100"))[0].serverList == "64.120.99.235,173.234.147.130")
    }

    @Test
    fun testGetOperation_notFound() {
        Assert.assertTrue(getValue(arophixDnsResponseDao.getLastDnsLocations("111")).isEmpty())
    }

    @Test
    fun testInsertArophixDnsResponses() = runBlocking {
        arophixDnsResponseDao.deleteButtonText(testButtonText)
        arophixDnsResponseDao.deleteDnsLocation(testDnsLocation)

        arophixDnsResponseDao.insertArophixDnsResponse(testButtonText, testDnsLocations)

        // Test read separately
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getButtonTexts()).size, CoreMatchers.equalTo(1))
        ViewMatchers.assertThat(getValue(arophixDnsResponseDao.getDnsLocations()).size, CoreMatchers.equalTo(6))
    }

    @Test fun testGetArophixDnsResponses() = runBlocking {
        arophixDnsResponseDao.deleteButtonText(testButtonText)
        arophixDnsResponseDao.deleteDnsLocation(testDnsLocation)

        arophixDnsResponseDao.insertArophixDnsResponse(testButtonText, testDnsLocations)

        // test read ArophixDnsResponse in transaction
        val arophixDnsResponses = getValue(arophixDnsResponseDao.getArophixDnsResponses())
        ViewMatchers.assertThat(arophixDnsResponses.size, CoreMatchers.equalTo(1))

        ViewMatchers.assertThat(arophixDnsResponses[0].buttonText.text, CoreMatchers.equalTo("Another text to be handled on client side."))
        ViewMatchers.assertThat(arophixDnsResponses[0].dnsLocations.size, CoreMatchers.equalTo(6))
    }

}

