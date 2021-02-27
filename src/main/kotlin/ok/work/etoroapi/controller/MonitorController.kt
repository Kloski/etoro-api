package ok.work.etoroapi.controller

import ok.work.etoroapi.client.EtoroHttpClient
import ok.work.etoroapi.client.EtoroMirrors
import ok.work.etoroapi.client.EtoroPosition
import ok.work.etoroapi.model.DailyChartData
import ok.work.etoroapi.model.UserDetail
import ok.work.etoroapi.model.YearMonthPerformance
import ok.work.etoroapi.model.ofString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/monitoring")
class MonitorController {

    @Autowired
    lateinit var httpClient: EtoroHttpClient


    @GetMapping("/detail")
    fun getPersonDetail(@RequestHeader(defaultValue = "Demo") mode: String, @RequestParam cid: String): UserDetail {
        return httpClient.getPersonDetail(ofString(mode), cid)
    }

    @GetMapping("/history")
    fun getPersonHistory(@RequestHeader(defaultValue = "Demo") mode: String, @RequestParam cid: String): YearMonthPerformance {
        return httpClient.getPersonHistory(ofString(mode), cid)
    }

    @GetMapping("/chart")
    fun getPersonChart(@RequestHeader(defaultValue = "Demo") mode: String, @RequestParam userName: String, @RequestParam period: String?): DailyChartData {
        return httpClient.getPersonDailyChartData(ofString(mode), userName, period)
    }

    @GetMapping("/portfolio")
    fun getMonitoredPersonData(@RequestHeader(defaultValue = "Demo") mode: String, @RequestParam cid: String): List<EtoroMirrors> {
        return httpClient.getPersonData(ofString(mode), cid)
    }


    @GetMapping("/data")
    fun getMonitoredPersonDataData(@RequestHeader(defaultValue = "Demo") mode: String, @RequestParam cidList: String): List<EtoroPosition> {
        return httpClient.getPersonDataData(ofString(mode), cidList)
    }

}

