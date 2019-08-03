package am.royalbank.uberbear.frameworks.sql

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import java.math.BigDecimal
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ConnectionExtKtTest {
    private val connection = mockk<Connection>()
    private val preparedStatement = mockk<PreparedStatement>(relaxed = true)

    @BeforeEach fun setup() {
        every { connection.prepareStatement(any()) } returns preparedStatement
    }

    @Test fun `verify sqlQuery parameter mapping and implementation`() {
        val param1 = BigDecimal("123.45")
        val param2 = UUID.randomUUID()
        val param3: Any? = null
        val param4 = "a string"
        connection.sqlQuery(
            "some sql", param1, param2, param3, param4) {}

        verifySequence {
            // check parameter mapping
            preparedStatement.setBigDecimal(1, param1)
            preparedStatement.setObject(2, param2)
            preparedStatement.setString(3, param3 as String?)
            preparedStatement.setString(4, param4)

            preparedStatement.executeQuery()
            preparedStatement.close()
        }
    }

    @Test fun `verify sqlUpdate parameter mapping and implementation`() {
        val param1 = BigDecimal("123.45")
        val param2 = UUID.randomUUID()
        val param3: Any? = null
        val param4 = "a string"
        connection.sqlUpdate(
            "some sql", param1, param2, param3, param4)

        verifySequence {
            // check parameter mapping
            preparedStatement.setBigDecimal(1, param1)
            preparedStatement.setObject(2, param2)
            preparedStatement.setString(3, param3 as String?)
            preparedStatement.setString(4, param4)

            preparedStatement.executeUpdate()
            preparedStatement.close()
        }
    }
}
