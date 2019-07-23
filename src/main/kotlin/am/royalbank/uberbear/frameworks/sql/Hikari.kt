package am.royalbank.uberbear.frameworks.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object Hikari {
    fun getDataSource(jdbcUrl: String, username: String, password: String, driverClassName: String? = null): DataSource =
        HikariDataSource(
            HikariConfig().apply {
                this.jdbcUrl = jdbcUrl
                this.username = username
                this.password = password
                if (driverClassName != null) {
                    this.driverClassName = driverClassName
                }
            })
}
