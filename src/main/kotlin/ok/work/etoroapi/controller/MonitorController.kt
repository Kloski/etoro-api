package ok.work.etoroapi.controller

import ok.work.etoroapi.client.EtoroHttpClient
import ok.work.etoroapi.client.EtoroMirrors
import ok.work.etoroapi.client.EtoroPosition
import ok.work.etoroapi.config.UserDataProperties
import ok.work.etoroapi.model.ofString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/monitoring")
class MonitorController {

    @Autowired
    lateinit var httpClient: EtoroHttpClient

    @Autowired
    private lateinit var userData: UserDataProperties


    @GetMapping
    fun getMonitoredPersonData(@RequestHeader(defaultValue = "Demo") mode: String): List<EtoroMirrors> {
        return httpClient.getPersonData(ofString(mode), userData.person_to_monitor_cid)
    }


    @GetMapping("/data")
    fun getMonitoredPersonDataData(@RequestHeader(defaultValue = "Demo") mode: String, @RequestParam cidList: String): List<EtoroPosition> {
        return httpClient.getPersonDataData(ofString(mode), cidList)
    }

}

