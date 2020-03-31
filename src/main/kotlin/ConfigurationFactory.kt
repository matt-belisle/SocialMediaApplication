
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.slf4j.LoggerFactory
import java.io.File

@Serializable
data class Configuration(
    val dbUsername: String,
    val dbPassword: String,
    val dbURL: String
)

object ConfigurationFactory {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var configuration: Configuration
    val json = Json(JsonConfiguration.Stable)

    init {
        //read and populate configuration

//        val fileName = "resources/Configuration.json"
        val config = ConfigurationFactory::class.java.getResource("Configuration.json").readText()

        configuration = json.parse(Configuration.serializer(), config)
        logger.debug("Configuration loaded as: $configuration")

    }
    // the one true source for a configuration
    fun getConfiguration() : Configuration =  configuration

}

