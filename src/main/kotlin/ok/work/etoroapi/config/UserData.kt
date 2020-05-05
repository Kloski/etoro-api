package ok.work.etoroapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "application.user")
data class UserDataProperties(
        var login: String = "",
        var email: String = "",
        var nickname: String = "",
        var pass: String = ""
)
