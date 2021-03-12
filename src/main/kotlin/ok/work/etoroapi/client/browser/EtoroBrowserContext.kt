package ok.work.etoroapi.client.browser

import ok.work.etoroapi.client.appVersion
import ok.work.etoroapi.client.appid
import ok.work.etoroapi.client.userAgent
import ok.work.etoroapi.config.UserDataProperties
import org.json.JSONException
import org.json.JSONObject
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.LogEntry
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.logging.Level
import javax.annotation.PostConstruct


data class EtoroMetadata(val cookies: String, val token: String, val lsPassword: String, val baseUrl: String, val domain: String)

@Component
class EtoroMetadataService(@Value("\${etoro.baseUrl}") val baseUrl: String, @Value("\${etoro.domain}") val domain: String) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var userData: UserDataProperties

    private lateinit var cookies: String
    private lateinit var token: String
    private lateinit var expirationTime: Date
    private lateinit var driver: ChromeDriver
    private lateinit var opts: ChromeOptions

    @PostConstruct
    fun init() {

        val pathToDriver: String = when {
            System.getProperty("os.name").startsWith("Mac") -> {
                "drivers/mac/chromedriver"
            }
            System.getProperty("os.name").toLowerCase().contains("windows") -> {
                "drivers/windows/chromedriver2.exe"
            }
            else -> {
                "drivers/ubuntu/chromedriver"
            }
        }

        // https://stackoverflow.com/questions/41460168/what-is-difference-between-xvfb-and-chromedriver-and-when-to-use-them
        opts = ChromeOptions()
        System.setProperty("webdriver.chrome.driver", pathToDriver)
        opts.addArguments("start-maximized")
        opts.addArguments("--no-sandbox"); // Bypass OS security model
        opts.addArguments("--disable-blink-features=AutomationControlled")
        // addLoggingParams()

        login()
    }

    fun login() {
        driver = ChromeDriver(opts)

        driver.get("$baseUrl/login")
        val email = userData.email
        val password = userData.pass
        if (email == null || password == null) {
            throw RuntimeException("LOGIN and/or PASSWORD environment variables are missing")
        }
        driver.findElementById("username").sendKeys(email)
        driver.findElementById("password").sendKeys(password)
        driver.findElementByClassName("blue-btn").click()
        var seconds = 0
        while (true) {
            try {
                Thread.sleep(1500)
                // logHttpParams()
                token = driver.executeScript("return JSON.parse(atob(window.localStorage.loginData)).stsData_app_1.accessToken;") as String
                logger.info("Token retrieved after %d seconds".format(seconds))
                break
            } catch (e: Exception) {
                if (seconds > 5) {
                    throw RuntimeException("Failed to retrieve token")
                }
                seconds++
            }
        }
        expirationTime = Date(driver.executeScript("return JSON.parse(atob(window.localStorage.loginData)).stsData_app_1.expirationUnixTimeMs;") as Long)
        logger.info(token)
        logger.info("expires at: $expirationTime")
        val cookiesSet = driver.manage().cookies
        cookies = cookiesSet.toList().joinToString("; ") { cookie -> "${cookie.name}=${cookie.value}" }
        logger.info("cookies: $cookies")

        driver.quit()
    }

    fun getMetadata(): EtoroMetadata {
        if (Date().after(expirationTime)) {
            login()
        }
        return EtoroMetadata(
                cookies,
                token,
                """{"UserAgent":"${userAgent}","ApplicationVersion":"${appVersion}","ApplicationName":"${appid}","AccountType":"Demo","ApplicationIdentifier":"${appid}"}""",
                baseUrl,
                domain
        )
    }

    private fun addLoggingParams() {
        // https://stackoverflow.com/questions/6509628/how-to-get-http-response-code-using-selenium-webdriver
        val cap = DesiredCapabilities.chrome()
        cap.setCapability(ChromeOptions.CAPABILITY, opts)
        // set performance logger
        // this sends Network.enable to chromedriver
        val logPrefs = LoggingPreferences()
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL)
        cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs)
    }

    private fun logHttpParams() {
        // https://stackoverflow.com/questions/6509628/how-to-get-http-response-code-using-selenium-webdriver

        // and capture the last recorded url (it may be a redirect, or the
        // original url)
        val currentURL: String = driver.currentUrl

        var status = -1

        // then ask for all the performance logs from this request
        // one of them will contain the Network.responseReceived method
        // and we shall find the "last recorded url" response
        val logs = driver.manage().logs().get("performance")

        val it: Iterator<LogEntry> = logs.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            try {
                val json = JSONObject(entry.message)
                println(json.toString())
                val message: JSONObject = json.getJSONObject("message")
                val method: String = message.getString("method")
                if (method != null && "Network.responseReceived".equals(method)) {
                    val params: JSONObject = message.getJSONObject("params")
                    val response: JSONObject = params.getJSONObject("response")
                    val messageUrl: String = response.getString("url")
                    if (currentURL.equals(messageUrl)) {
                        status = response.getInt("status")
                        println("---------- bingo !!!!!!!!!!!!!! returned response for " + messageUrl + ": " + status)
                        println("---------- bingo !!!!!!!!!!!!!! headers: " + response.get("headers"))
                    }
                }
            } catch (e: JSONException) {
            }
        }
    }
}
