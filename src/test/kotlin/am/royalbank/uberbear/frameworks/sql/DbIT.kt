package am.royalbank.uberbear.frameworks.sql

import am.royalbank.uberbear.frameworks.testcontainers.KPostgreSQLContainer
import org.junit.jupiter.api.Test

internal class DbIT {

    @Test fun `verify that database definition is PostgreSQL compatible`() {
        KPostgreSQLContainer().use { postgres ->
            postgres.start()
            Db.init(postgres.jdbcUrl, postgres.username, postgres.password)
        }
    }
}
