package ok.work.etoroapi.client

import ok.work.etoroapi.client.browser.EtoroMetadata
import ok.work.etoroapi.model.TradingMode
import okhttp3.Request
import java.net.URI
import java.net.http.HttpRequest
import kotlin.math.round

fun prepareRequest(path: String, auth: String, mode: TradingMode, credentials: EtoroMetadata): HttpRequest.Builder {
    return HttpRequest.newBuilder().uri(URI("${credentials.baseUrl}/${path}"))
            .header("authority", credentials.domain)
            .header("accounttype", mode.name)
            .header("x-sts-appdomain", credentials.baseUrl)
            .header("content-type", "application/json;charset=UTF-8")
            .header("accept", "application/json, text/plain, */*")
            .header("x-sts-gatewayappid", "90631448-9A01-4860-9FA5-B4EBCDE5EA1D")
            .header("applicationidentifier", "ReToro")
            .header("applicationversion", "212.0.7")
            .header("origin", credentials.baseUrl)
            .header("sec-fetch-site", "same-origin")
            .header("sec-fetch-mode", "cors")
            .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36")
            .header("authorization", auth)
            .header("referer", "${credentials.baseUrl}/login")
            .header("cookie", credentials.cookies)
}

fun prepareRequest2(path: String, auth: String, mode: TradingMode, credentials: EtoroMetadata): HttpRequest.Builder {
    return HttpRequest.newBuilder().uri(URI("${credentials.baseUrl}/${path}"))
        .header("origin", credentials.baseUrl)
        .header("authority", credentials.domain)
        .header("x-sts-appdomain", credentials.baseUrl)
        .header("x-sts-gatewayappid", "90631448-9A01-4860-9FA5-B4EBCDE5EA1D")
        .header("content-type", "application/json;charset=UTF-8")
        .header("accept", "application/json, text/plain, */*")
        .header("accept-language", "sk-SK,sk;q=0.9")
        .header("accounttype", mode.name)
        .header("applicationidentifier", "ReToro")
        .header("applicationversion", "312.0.2")
        .header("authorization", auth)
        .header("cookie", credentials.cookies)
        .header("dnt", "1")
        .header("referer", "${credentials.baseUrl}/login")
        .header("sec-ch-ua", "Chromium\";v=\"88\", \"Google Chrome\";v=\"88\", \";Not A Brand\";v=\"99")
        .header("sec-ch-ua-mobile", "?0")
        .header("sec-fetch-dest", "empty")
        .header("sec-fetch-mode", "cors")
        .header("sec-fetch-site", "same-origin")
        .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36")
        .header("x-csrf-token", credentials.token)
}

fun prepareOkRequest(path: String, auth: String, mode: TradingMode, credentials: EtoroMetadata): Request.Builder {
    return Request.Builder().url("${credentials.baseUrl}/${path}")
            .header("authority", credentials.domain)
            .header("accounttype", mode.name)
            .header("x-sts-appdomain", credentials.baseUrl)
            .header("content-type", "application/json;charset=UTF-8")
            .header("accept", "application/json, text/plain, */*")
            .header("x-sts-gatewayappid", "90631448-9A01-4860-9FA5-B4EBCDE5EA1D")
            .header("applicationidentifier", "ReToro")
            .header("applicationversion", "212.0.7")
            .header("origin", credentials.baseUrl)
            .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
            .header("sec-fetch-site", "same-origin")
            .header("sec-fetch-mode", "cors")
            .header("authorization", auth)
            .header("referer", "${credentials.baseUrl}/login")
            .header("cookie", credentials.cookies)
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}