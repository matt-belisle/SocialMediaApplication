package dataBase

import ConfigurationFactory
import dataBase.tables.AusPol2019Data
import dataBase.tables.AusPol2019DataTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

//Single source for working with databases, always call dbQuery when wanting to hit the database
object DatabaseFactory {
    private val logger = mu.KotlinLogging.logger {}
    // call this before any DB calls to set up DB connection
    fun init() {
        val config = ConfigurationFactory.getConfiguration()
        Database.connect(url = "jdbc:mysql://${config.dbURL}?useSSL=false&rewriteBatchedInserts=true&useSSL=false&allowPublicKeyRetrieval=true", driver = "com.mysql.cj.jdbc.Driver", user = config.dbUsername, password = config.dbPassword)
        //TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }

    // this will execute a dbQuery asynchronously as a transaction
    suspend fun <T> dbQuery(
        block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}


suspend fun main(){
    DatabaseFactory.init()
    val logger = mu.KotlinLogging.logger {}
    DatabaseFactory.dbQuery {
        val data = AusPol2019DataTable.selectAll().limit(1).map {
            AusPol2019Data(
                it
            )
        }
        logger.info { data[0] }
    }
}