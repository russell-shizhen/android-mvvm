package com.arophix.mvvm.example

import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.arophix.mvvm.example.data.AppDatabase
import com.arophix.mvvm.example.data.ArophixDnsResponseDao
import okhttp3.mockwebserver.*
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Kotlin reflection utility methods to fix MockWebServer bug:
 * https://stackoverflow.com/questions/63387807/java-lang-illegalargumentexception-log-tag-okhttp3-mockwebserver-mockwebserver
 *
 * The solution is to tweak with below property of [MockWebServer]
 *
 * ```kotlin
 * private val logger = Logger.getLogger(MockWebServer::class.java.name)
 * ```
 * MockWebServer::class.java.name will return the full class name that usually is too long for an Android log tag,
 * Using reflection to replace it with MockWebServer::class.java.simpleName will fix the issue.
 */
fun <T : Any> T.getPrivateProperty(variableName: String): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

fun <T : Any> T.setAndReturnPrivateProperty(variableName: String, data: Any): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        field.set(this, data)
        return@let field.get(this)
    }
}

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val mockWebServer = MockWebServer()

    private val successDispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val responseBody = AssetReaderUtil.asset(context, "arophix_response_success.xml")
            return MockResponse()
                    //.throttleBody(1024, 5, TimeUnit.SECONDS)
                    .setResponseCode(200)
                    .setBody(responseBody)
        }
    }

    private val error403Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val responseBody = AssetReaderUtil.asset(context, "arophix_response_error.xml")
            return MockResponse()
                    .throttleBody(1024, 5, TimeUnit.SECONDS)
                    .setResponseCode(403)
                    .setBody(responseBody)
        }
    }

    private val errorTimeoutDispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
        }
    }

    private val error404Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return MockResponse()
                    .throttleBody(1024, 5, TimeUnit.SECONDS)
                    .setResponseCode(404)
                    .setBody("Not found")
        }
    }

    @Rule @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java, true, false)

    @get:Rule
    var okHttpIdlingResourceRule = OkHttpIdlingResourceRule()

    private lateinit var arophixDnsResponseDao: ArophixDnsResponseDao

    @Before
    fun setup() {
        IdlingPolicies.setMasterPolicyTimeout(3, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(3, TimeUnit.MINUTES)

        mockWebServer.setAndReturnPrivateProperty("logger", Logger.getLogger(MockWebServer::class.java.simpleName))

        mockWebServer.start(8080)
        arophixDnsResponseDao = AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().targetContext).arophixDnsResponseDao()

        DnsLocationListFragment.isForTesting = true
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    /**
     * 1. Upon activity launch, snack bar does not show up.
     * 2. Clean database, refresh button shall be original text, i.e. REFRESH
     * 3. Perform click on refresh button, button text shall change to "Another text to be handled on client side. xxx"
     * 4. Recycler view shall become visible and item count is 6 (as predefined in test vector)
     * 5. Perform second click, button text shall change.
     */
    @Test fun getLocationList_Success() {

        arophixDnsResponseDao.cleanDnsLocationsTable()
        arophixDnsResponseDao.cleanButtonTextsTable()

        mockWebServer.dispatcher = successDispatcher

        activityTestRule.launchActivity(null)
        onView(withId(R.id.refresh)).check(matches(withText("REFRESH")));
        onView(withId(R.id.showBestLocation)).check(matches(withText("SHOW BEST LOCATION")))

        val recyclerView: RecyclerView = activityTestRule.activity.findViewById(R.id.dns_location_list)
        assert(recyclerView.adapter!!.itemCount == 0)

        onView(withId(R.id.refresh)).perform(click())
        onView(withId(R.id.refresh)).check(matches(withSubstring("Another text to be handled on client side.")))
        onView(withId(R.id.dns_location_list)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        assert(recyclerView.adapter!!.itemCount == 6)

        val refreshButton: Button = activityTestRule.activity.findViewById(R.id.refresh)
        val firstClick_ButtonText: String = refreshButton.text.toString()

        onView(withId(R.id.refresh)).perform(click())
        onView(withId(R.id.refresh)).check(matches(withSubstring("Another text to be handled on client side.")))
        onView(withId(R.id.dns_location_list)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        val secondClick_ButtonText: String = refreshButton.text.toString()

        assert(firstClick_ButtonText != secondClick_ButtonText)
    }

    /**
     * Upon activity launch, snack bar displays 403 error
     * User click on refresh button, snack bar displays 403 error and button text shall not change
     */
    @Test fun snackbar_Show_Network_403_Error() {

        mockWebServer.dispatcher = error403Dispatcher

        activityTestRule.launchActivity(null)

//        onView(snackBar403Matcher).check(matches(isDisplayed()))

        val refreshButton: Button = activityTestRule.activity.findViewById(R.id.refresh)
        val beforeClick_ButtonText: String = refreshButton.text.toString()

        onView(withId(R.id.refresh)).perform(click())

        onView(snackBar403Matcher).check(matches(isDisplayed()))

        val afterClick_ButtonText: String = refreshButton.text.toString()

        assert(beforeClick_ButtonText == afterClick_ButtonText)
    }

    @Test fun snackbar_Show_Network_Timeout_Error() {

        mockWebServer.dispatcher = errorTimeoutDispatcher
        activityTestRule.launchActivity(null)

//        onView(snackBarTimeoutMatcher).check(matches(isDisplayed()))

        val refreshButton: Button = activityTestRule.activity.findViewById(R.id.refresh)
        val beforeClick_ButtonText: String = refreshButton.text.toString()

        onView(withId(R.id.refresh)).perform(click())

        onView(snackBarTimeoutMatcher).check(matches(isDisplayed()))

        val afterClick_ButtonText: String = refreshButton.text.toString()

        assert(beforeClick_ButtonText == afterClick_ButtonText)
    }

    /**
     * Upon activity launch, snack bar displays 404 error
     * User click on refresh button, snack bar displays 404 error and button text shall not change
     */
    @Test fun snackbar_Show_Network_404_Error() {

        mockWebServer.dispatcher = error404Dispatcher

        activityTestRule.launchActivity(null)

//        onView(snackBar404Matcher).check(matches(isDisplayed()))

        val refreshButton: Button = activityTestRule.activity.findViewById(R.id.refresh)
        val beforeClick_ButtonText: String = refreshButton.text.toString()

        onView(withId(R.id.refresh)).perform(click())

        onView(snackBar404Matcher).check(matches(isDisplayed()))

        val afterClick_ButtonText: String = refreshButton.text.toString()

        assert(beforeClick_ButtonText == afterClick_ButtonText)
    }

    private val snackBar403Matcher = allOf(
            withId(com.google.android.material.R.id.snackbar_text),
            withSubstring("403 Client Error")
    )

    private val snackBar404Matcher = allOf(
            withId(com.google.android.material.R.id.snackbar_text),
            withSubstring("404 Client Error")
    )

    private val snackBarTimeoutMatcher = allOf(
            withId(com.google.android.material.R.id.snackbar_text),
            withSubstring("timeout")
    )


    private val snackBarAllMatcher = allOf(
            withId(com.google.android.material.R.id.snackbar_text),
            withSubstring("Network call has failed for a following reason: ")
    )
}
