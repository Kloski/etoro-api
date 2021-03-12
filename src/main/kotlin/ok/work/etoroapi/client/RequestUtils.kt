package ok.work.etoroapi.client

import ok.work.etoroapi.client.browser.EtoroMetadata
import ok.work.etoroapi.model.TradingMode
import okhttp3.Request
import java.net.URI
import java.net.http.HttpRequest
import kotlin.math.round

val userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36"
val gatewayappid = "90631448-9A01-4860-9FA5-B4EBCDE5EA1D"
val contentType = "application/json;charset=UTF-8"
val accept = "application/json, text/plain, */*"
val appid = "ReToro"
val appVersion = "312.0.2"
val secChUa = "Chromium\";v=\"88\", \"Google Chrome\";v=\"88\", \";Not A Brand\";v=\"99"
val uaMobile = "?0"
val fetchDestination = "empty"
val fetchMode = "cors"
val fetchSite = "same-origin"

fun prepareRequest(path: String, auth: String, mode: TradingMode, credentials: EtoroMetadata): HttpRequest.Builder {
    return HttpRequest.newBuilder().uri(URI("${credentials.baseUrl}/${path}"))
        .header("origin", credentials.baseUrl)
        .header("authority", credentials.domain)
        .header("x-sts-appdomain", credentials.baseUrl)
        .header("x-sts-gatewayappid", gatewayappid)
        .header("content-type", contentType)
        .header("accept", accept)
        .header("accounttype", mode.name)
        .header("applicationidentifier", appid)
        .header("applicationversion", appVersion)
        .header("authorization", auth)
        .header("cookie", credentials.cookies)
        .header("referer", "${credentials.baseUrl}/login")
        .header("sec-ch-ua", secChUa)
        .header("sec-ch-ua-mobile", uaMobile)
        .header("sec-fetch-dest", fetchDestination)
        .header("sec-fetch-mode", fetchMode)
        .header("sec-fetch-site", fetchSite)
        .header("user-agent", userAgent)
}

fun prepareRequest2(path: String, auth: String, mode: TradingMode, credentials: EtoroMetadata): HttpRequest.Builder {
    return HttpRequest.newBuilder().uri(URI("${credentials.baseUrl}/${path}"))
        .header("origin", credentials.baseUrl)
        .header("authority", credentials.domain)
        .header("x-sts-appdomain", credentials.baseUrl)
        .header("x-sts-gatewayappid", gatewayappid)
        .header("content-type", contentType)
        .header("accept", accept)
        .header("accept-language", "sk-SK,sk;q=0.9")
        .header("accounttype", mode.name)
        .header("applicationidentifier", appid)
        .header("applicationversion", appVersion)
        .header("authorization", auth)
        .header("cookie", credentials.cookies)
        .header("dnt", "1")
        .header("referer", "${credentials.baseUrl}/login")
        .header("sec-ch-ua", secChUa)
        .header("sec-ch-ua-mobile", uaMobile)
        .header("sec-fetch-dest", fetchDestination)
        .header("sec-fetch-mode", fetchMode)
        .header("sec-fetch-site", fetchSite)
        .header("user-agent", userAgent)
        .header("x-csrf-token", credentials.token)
}

fun prepareOkRequest(path: String, auth: String, mode: TradingMode, credentials: EtoroMetadata): Request.Builder {
    return Request.Builder().url("${credentials.baseUrl}/${path}")
        .header("origin", credentials.baseUrl)
        .header("authority", credentials.domain)
        .header("x-sts-appdomain", credentials.baseUrl)
        .header("x-sts-gatewayappid", gatewayappid)
        .header("content-type", contentType)
        .header("accept", accept)
        .header("accounttype", mode.name)
        .header("applicationidentifier", appid)
        .header("applicationversion", appVersion)
        .header("authorization", auth)
        .header("cookie", credentials.cookies)
        .header("referer", "${credentials.baseUrl}/login")
        .header("sec-ch-ua", secChUa)
        .header("sec-ch-ua-mobile", uaMobile)
        .header("sec-fetch-dest", fetchDestination)
        .header("sec-fetch-mode", fetchMode)
        .header("sec-fetch-site", fetchSite)
        .header("user-agent", userAgent)
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}