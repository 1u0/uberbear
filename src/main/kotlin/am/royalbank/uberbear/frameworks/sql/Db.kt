package am.royalbank.uberbear.frameworks.sql

import java.sql.Connection
import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

class Db(
    private val dataSource: DataSource
) {

    fun <R> connect(autoCommit: Boolean = true, block: (Connection) -> R): R =
        dataSource.connection.use {
            if (autoCommit) {
                return block(it)
            }
            val oldAutoCommit = it.autoCommit
            try {
                it.autoCommit = false
                val result = block(it)
                it.commit()
                return result
            } catch (ex: Throwable) {
                it.rollback()
                it.autoCommit = oldAutoCommit
                throw ex
            }
        }

    companion object {
        private val logger = LoggerFactory.getLogger(Db::class.java)

        fun init(jdbcUrl: String, username: String, password: String): Db {
            val dataSource = Hikari.getDataSource(jdbcUrl, username, password)
            Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate()

            logger.info("init database - done")

            return Db(dataSource)
        }
    }
}
